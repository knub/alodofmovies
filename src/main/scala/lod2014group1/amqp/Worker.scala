package lod2014group1.amqp

import com.rabbitmq.client._
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory
import java.util.UUID

object Worker {
	private val conf = ConfigFactory.load();
	private val QUEUE_NAME = conf.getString("alodofmovies.hosts.tukex.queue")
	private val HOST_NAME = conf.getString("alodofmovies.hosts.tukex.host")
	private val VHOST = conf.getString("alodofmovies.hosts.tukex.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.tukex.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.tukex.password")

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

		val rpcClient = new RPCClient()

		println(" [*] Waiting for messages. To exit press CTRL+C")
		while (true) {
			val delivery = consumer.nextDelivery()
			val task = delivery.getBody.unpickle[WorkerTask]
			println(" [x] Received '" + task.msg + "'")
			val answer = doWork()
			println(" [x] Done working on '" + task.msg + "'")
			val ack = rpcClient.send(answer)
			if (ack) {
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false)
				println(" [x] Done with '" + task.msg + "'")
			} else {
				println(" [Error] Something went wrong.'")
			}

		}
	}

	def doWork(task: WorkerTask): String = {
		Thread.sleep(task.time * 1000)
		task.msg.toLowerCase()
	}
}

object RPCClient {
	private val conf = ConfigFactory.load()
	private val QUEUE_NAME = "answers"
	private val HOST_NAME = conf.getString("alodofmovies.hosts.tukex.host")
	private val VHOST = conf.getString("alodofmovies.hosts.tukex.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.tukex.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.tukex.password")
}

class RPCClient {
	val factory = new ConnectionFactory()
	factory.setHost(RPCClient.HOST_NAME)

	factory.setVirtualHost(RPCClient.VHOST)
	factory.setUsername(RPCClient.USERNAME)
	factory.setPassword(RPCClient.PASSWORD)

	private var connection: Connection = factory.newConnection()
	private var channel: Channel = connection.createChannel()
	private var replyQueueName: String = channel.queueDeclare().getQueue
	private var consumer: QueueingConsumer = new QueueingConsumer(channel)

	channel.basicConsume(replyQueueName, true, consumer)

	def send(msg: String): Boolean = {
		val corrId = UUID.randomUUID().toString
		val props = new BasicProperties.Builder().correlationId(corrId)
			.replyTo(replyQueueName)
			.build()
		println("[x] Sending rpc'" + msg + "'")
		channel.basicPublish("", RPCClient.QUEUE_NAME, props, msg.pickle.value)
		var recievedAnswer = false
		println("[x] Waiting for rpc answer")
		while (recievedAnswer) {
			val delivery = consumer.nextDelivery()
			if (delivery.getProperties.getCorrelationId == corrId) {
				recievedAnswer = delivery.getBody.unpickle[Boolean]
			}
		}
		recievedAnswer
	}

	def close() {
		connection.close()
	}
}
