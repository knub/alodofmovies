package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import com.typesafe.config.ConfigFactory
import lod2014group1.rdf.RdfTripleString
import org.slf4s.Logger
import org.slf4s.LoggerFactory
import lod2014group1.database.Task

case class WorkerTask(`type`: String, params: Map[String, String])

object WorkerTask {
	def fromDatabaseTask(dbTask: Task): WorkerTask = {
		WorkerTask(dbTask.taskType, Map("task_id" -> dbTask.id.toString, "uri" -> dbTask.fileOrUrl))
	}
}
case class UriFile(uri: String, fileContent: String)
case class TaskAnswer(header: String, files: List[UriFile], triples: List[RdfTripleString])


object ConnectionBuilder {
	private val conf = ConfigFactory.load()
	private val HOST_NAME = conf.getString("alodofmovies.hosts.server.host")
	private val VHOST = conf.getString("alodofmovies.hosts.server.vhost")
	private val USERNAME = conf.getString("alodofmovies.hosts.server.username")
	private val PASSWORD = conf.getString("alodofmovies.hosts.server.password")

	def newConnection(): Connection = {
		val factory = new ConnectionFactory()
		factory.setHost(HOST_NAME)
		factory.setVirtualHost(VHOST)
		factory.setUsername(USERNAME)
		factory.setPassword(PASSWORD)
		factory.newConnection
	}
}

class Supervisor() {
	val taskQueueName = "tasks"
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

class RPCServer(rpcQueueName: String) extends Runnable {
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

	var answersReceived = 0
	val answersLog: Logger = LoggerFactory.getLogger("TaskAnswerLogger")
	def handle(messageBody: Array[Byte]): Unit = {
		val answer = messageBody.unpickle[TaskAnswer]
		answersReceived += 1

		answersLog.info("Received '" + answer.header + "'")
		answersLog.info(s"Safed ${answer.files.size} files.")
		answersLog.info(answer.files(0).fileContent)
		answersLog.info(s"Stored ${answer.triples.size} files.")
	}
}

