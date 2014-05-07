package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._

case class WorkerTask(msg: String, freq: Int)

/**
 * An Example of how to use the Example subclass of AMQPSender[T]. Still following?
 */
object Send {
	private val QUEUE_NAME = "hello"

	def send(msg: String) {
		val message = WorkerTask(msg, 4)
		def printMessage(): Unit = println(" [x] Received '" + msg + "'")
		val factory = new ConnectionFactory()
		factory.setHost("localhost")
		val connection = factory.newConnection()
		val channel = connection.createChannel()
		channel.queueDeclare(QUEUE_NAME, false, false, false, null)
		channel.basicPublish("", QUEUE_NAME, null, message.pickle.value)
		println(" [x] Sent '" + msg + "'")
		channel.close()
		connection.close()
	}
}
