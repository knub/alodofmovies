package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory

object Worker {
	private val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.localhost.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.localhost.host")
	private val VHOST = conf.getString("alodofmovies.hosts.localhost.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.localhost.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.localhost.password")

	def listen() {
		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(VHOST)
		factory.setUsername(USERNAME)
		factory.setPassword(PASSWORD)

		val connection = factory.newConnection()
		val channel = connection.createChannel()
		val durable = true
		channel.queueDeclare(QUEUE_NAME, durable, false, false, null)

		val consumer = new QueueingConsumer(channel)
		channel.basicQos(1)
		val autoAck = false
		channel.basicConsume(QUEUE_NAME, autoAck, consumer)

		println(" [*] Waiting for messages. To exit press CTRL+C")
		while (true) {
			val delivery = consumer.nextDelivery();
			val task = delivery.getBody.unpickle[WorkerTask]
			println(" [x] Received '" + task.msg + "'")
			doWork(task);
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			System.out.println(" [x] Done with '" + task.msg + "'");
		}
	}

	def doWork(task: WorkerTask) {
		Thread.sleep(task.time * 1000)
	}
}
