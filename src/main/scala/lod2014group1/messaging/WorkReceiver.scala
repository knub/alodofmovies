package lod2014group1.messaging

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import java.util.UUID
import org.slf4s._
import scala.util.{Failure, Success, Try}
import TaskType._
import lod2014group1.messaging.worker._
import lod2014group1.messaging.worker.TaskAnswer
import net.liftweb.json.Serialization.{read, write}


class WorkReceiver(taskQueueName: String, answerQueueName: String) {

	val workerAssignment = Map(Triplify -> classOf[TriplifyWorker], Crawl -> classOf[CrawlWorker])
	var log: Logger = _
	var consumer: QueueingConsumer = _
	var rpcClient: RPCClient = _
	var channel: Channel = _
	var connection: Connection = _
	implicit val formats = net.liftweb.json.DefaultFormats

	def init(): Unit = {
		log = LoggerFactory.getLogger("TaskAnswerLogger")
		val connection = ConnectionBuilder.newConnection()
		channel = connection.createChannel()
		channel.queueDeclare(taskQueueName, true, false, false, null)
		channel.basicQos(1)
		consumer = new QueueingConsumer(channel)
		channel.basicConsume(taskQueueName, false, consumer)

		rpcClient = new RPCClient(answerQueueName)
	}

	def listen() {
		println("Waiting for messages. To exit press CTRL+C")
		var i = 0
		while (true) {
			i += 1
			val delivery = consumer.nextDelivery(5000)
			if (delivery != null) {
				val task = read[WorkerTask](new String(delivery.getBody, "UTF-8"))

				log.info(s"Task received: ${task.`type`}, id: ${task.taskId}, params: ${task.params.-("content")}}")
				val answer = Try(forwardTask(task))

				answer match {
					case Success(a) =>
						rpcClient.send(a)
					case Failure(e) =>
						log.error(e.getStackTraceString)
				}
				channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
			} else {
				log.warn("Timeouted.")
			}
//			if (i % 10000 == 0)
//				println(i)
		}
	}

	def close() {
		rpcClient.close()
		channel.close()
		connection.close()
	}

	def forwardTask(task: WorkerTask): TaskAnswer = {
		val worker: Worker = try {
			val taskName = TaskType.withName(task.`type`)
			workerAssignment.getOrElse(taskName, classOf[DummyWorker]).newInstance()
		} catch {
			case e: Throwable => new DummyWorker
		}
		worker.execute(task.taskId, task.params)
	}
}

class RPCClient(rpcQueueName: String) extends Logging {
	private val connection = ConnectionBuilder.newConnection()
	private val channel = connection.createChannel()
	private val replyQueueName = channel.queueDeclare().getQueue
	private val consumer = new QueueingConsumer(channel)
	channel.basicConsume(replyQueueName, true, consumer)

	implicit val formats = net.liftweb.json.DefaultFormats

	def send(taskAnswer: TaskAnswer): Unit = {
		val corrId = UUID.randomUUID().toString
		val props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build()

		val pickled = write(taskAnswer).getBytes("UTF-8")
		try {
			read[TaskAnswer](new String(pickled, "UTF-8"))
		} catch {
			case _: Throwable =>
				println("It failed for")
				println(taskAnswer.header)
				println(taskAnswer.taskId)
				println(taskAnswer.triples)
		}
		channel.basicPublish("", rpcQueueName, props, pickled)

		var receivedAnswer = false
		while (!receivedAnswer) {
			val delivery = consumer.nextDelivery()
			if (delivery.getProperties.getCorrelationId == corrId) {
				receivedAnswer = new String(delivery.getBody, "UTF-8") == "true"
			}
		}
	}

	def close() {
		channel.close()
		connection.close()
	}
}
