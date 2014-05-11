package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory

object Recv {
	val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.localhost.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.localhost.host")

	def listen() {
		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(conf.getString("alodofmovies.hosts.localhost.vhost"))
		factory.setUsername(conf.getString("alodofmovies.hosts.localhost.username"))
		factory.setPassword(conf.getString("alodofmovies.hosts.localhost.password"))

		val connection = factory.newConnection()
		val channel = connection.createChannel()
		channel.queueDeclare(QUEUE_NAME, false, false, false, null)
		println(" [*] Waiting for messages. To exit press CTRL+C")
		val consumer = new QueueingConsumer(channel)
		channel.basicConsume(QUEUE_NAME, true, consumer)
		while (true) {
			val delivery = consumer.nextDelivery()
			val message = delivery.getBody.unpickle[WorkerTask]
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			println(" [x] Received '" + message.msg + "'")
		}
	}
}