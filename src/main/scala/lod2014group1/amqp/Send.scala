package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory

case class WorkerTask(msg: String, freq: Int)

/**
 * An Example of how to use the Example subclass of AMQPSender[T]. Still following?
 */
object Send {
	val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.localhost.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.localhost.host")

	def send(msg: String) {
		val message = WorkerTask(msg, 4)

		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(conf.getString("alodofmovies.hosts.localhost.vhost"))
		factory.setUsername(conf.getString("alodofmovies.hosts.localhost.username"))
		factory.setPassword(conf.getString("alodofmovies.hosts.localhost.password"))

		val connection = factory.newConnection()
		val channel = connection.createChannel()
		channel.queueDeclare(QUEUE_NAME, false, false, false, null)
		channel.basicPublish("", QUEUE_NAME, null, message.pickle.value)
		println(" [x] Sent '" + msg + "'")
		channel.close()
		connection.close()
	}
}
