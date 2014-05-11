package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory

case class WorkerTask(msg: String, time: Int)

/**
 * An Example of how to use the Example subclass of AMQPSender[T]. Still following?
 */
object Supervisor {
	private val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.localhost.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.localhost.host")
	private val VHOST = conf.getString("alodofmovies.hosts.localhost.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.localhost.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.localhost.password")

	def send(task: WorkerTask) {

		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)

		factory.setVirtualHost(VHOST)
		factory.setUsername(USERNAME)
		factory.setPassword(PASSWORD)

		val connection = factory.newConnection()
		val channel = connection.createChannel()
		val durable = true
		channel.queueDeclare(QUEUE_NAME, durable, false, false, null)
		channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, task.pickle.value)
		println(" [x] Sent '" + task.msg + "' to queue '" + QUEUE_NAME + "' at vhost: '" + VHOST + "'")
		channel.close()
		connection.close()
	}
}
