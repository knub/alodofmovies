package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._


object Recv {

	private val QUEUE_NAME = "hello"

	def listen() {
		val factory = new ConnectionFactory()
		factory.setHost("localhost")
		val connection = factory.newConnection()
		val channel = connection.createChannel()
		channel.queueDeclare(QUEUE_NAME, false, false, false, null)
		println(" [*] Waiting for messages. To exit press CTRL+C")
		val consumer = new QueueingConsumer(channel)
		channel.basicConsume(QUEUE_NAME, true, consumer)
		while (true) {
			val delivery = consumer.nextDelivery()
			val message = delivery.getBody.unpickle[WorkerTask]
			println(" [x] Received '" + message.msg + "'")
		}
	}
}