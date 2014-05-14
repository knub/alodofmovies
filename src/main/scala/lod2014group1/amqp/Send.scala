package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory

case class WorkerTask(msg: String, time: Int)

/**
 * An Example of how to use the Example subclass of AMQPSender[T]. Still following?
 */
object Supervisor {
	private val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.tukex.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.tukex.host")
	private val VHOST = conf.getString("alodofmovies.hosts.tukex.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.tukex.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.tukex.password")

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

object RPCServer {
	private val conf = ConfigFactory.load();
	private val RPC_QUEUE_NAME = "answers"
	private val HOST_NAME = conf.getString("alodofmovies.hosts.tukex.host")
	private val VHOST = conf.getString("alodofmovies.hosts.tukex.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.tukex.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.tukex.password")
}

class RPCServer extends Runnable{

	override def run() {
		val factory = new ConnectionFactory()
		factory.setHost(RPCServer.HOST_NAME)
		factory.setVirtualHost(RPCServer.VHOST)
		factory.setUsername(RPCServer.USERNAME)
		factory.setPassword(RPCServer.PASSWORD)



		val connection = factory.newConnection()
		val channel = connection.createChannel()
		channel.queueDeclare(RPCServer.RPC_QUEUE_NAME, false, false, false, null)
		channel.basicQos(1)
		val consumer = new QueueingConsumer(channel)
		channel.basicConsume(RPCServer.RPC_QUEUE_NAME, false, consumer)
		println(" [x] Awaiting RPC requests")
		while (true) {
			val delivery = consumer.nextDelivery()
			val props = delivery.getProperties
			val replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId)
				.build()

			val answer = delivery.getBody.unpickle[String]
			handleAnswer(answer)
			val response = true
			channel.basicPublish("", props.getReplyTo, replyProps, response.getBytes("UTF-8"))
			channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
		}
	}

	def handleAnswer(answer: String) {
		println(" [x] Received '" + answer + "'")
	}

}

