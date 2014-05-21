package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory
import lod2014group1.rdf.RdfTriple
import lod2014group1.amqp.TaskType._

case class WorkerTask(`type`: String, params: Map[String, String])
case class UriFile(uri: String, fileContent: String)
case class TaskAnswer(header: String, files: List[UriFile], triples: List[RdfTriple])


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

			val response = handle(delivery.getBody)

			val props = delivery.getProperties
			val replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId).build()
			channel.basicPublish("", props.getReplyTo, replyProps, response.pickle.value)
			channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
		}
	}

	def close() {
		channel.close()
		connection.close()
	}

	def handle(messageBody: Array[Byte]): Boolean = {
		val answer = messageBody.unpickle[TaskAnswer]
		println(" [x] Received '" + answer.header + "'")
		println("I save these files:")
		answer.files.foreach(println)
		println("I stored these triples:")
		answer.triples.foreach(println)
		println()
	}
}

