package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory
import lod2014group1.rdf.RdfTripleString

case class WorkerTask(`type`: String, params: Map[String, String])
case class UriFile(uri: String, fileContent: String)
case class TaskAnswer(header: String, files: List[UriFile], triples: List[RdfTripleString])


object ConnectionBuilder {
	private val conf = ConfigFactory.load();
	private val HOST_NAME = conf.getString("alodofmovies.hosts.tukex.host")
	private val VHOST = conf.getString("alodofmovies.hosts.tukex.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.tukex.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.tukex.password")

	def newConnection(): Connection = {
		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(VHOST)
		factory.setUsername(USERNAME)
		factory.setPassword(PASSWORD)
		factory.newConnection
	}
}

class Supervisor(taskQueueName: String) {
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(taskQueueName, true, false, false, null)

	def send(task: WorkerTask) {
		channel.basicPublish("", taskQueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, task.pickle.value)
		println(" [x] Sent '" + task.`type` + "' to queue '" + taskQueueName + "'")
	}

	def close() {
		channel.close()
		connection.close()
	}
}

class RPCServer(rpcQueueName: String) extends Runnable{
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(rpcQueueName, false, false, false, null)
	channel.basicQos(1)
	val consumer = new QueueingConsumer(channel)
	channel.basicConsume(rpcQueueName, false, consumer)

	override def run() {

		println(" [x] Awaiting RPC requests")
		while (true) {
			val delivery = consumer.nextDelivery()

			handle(delivery.getBody)

			val props = delivery.getProperties
			val replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId).build()
			channel.basicPublish("", props.getReplyTo, replyProps, true.pickle.value)
			channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
		}
	}

	def close() {
		channel.close()
		connection.close()
	}

	var i = 0;
	def handle(messageBody: Array[Byte]): Unit = {
		val answer = messageBody.unpickle[TaskAnswer]
		i += 1;


		if (i % 701 == 35) {
			println(" [x] Received '" + answer.header + "'")
			println("I save these files:")
			println(answer.files.size)
			println("I stored these triples:")
			println(answer.triples.size)
			println("=======================")
		}
	}
}

