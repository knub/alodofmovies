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

	val workerAssignment = Map(
		Triplify -> classOf[TriplifyWorker],
		Crawl -> classOf[CrawlWorker],
		Crawlify -> classOf[CrawlifyWorker],
		Triplimerge -> classOf[TriplimergeWorker],
		Crawlifymerge -> classOf[CrawlifymergeWorker],
    Match -> classOf[MatchWorker]
	)
	var log: Logger = _
	var consumer: QueueingConsumer = _
	var listenChannel: Channel = _
	var sendChannel: Channel = _
	var connection: Connection = _
	implicit val formats = net.liftweb.json.DefaultFormats

	def init(): Unit = {
		log = LoggerFactory.getLogger("TaskAnswerLogger")
		val connection = ConnectionBuilder.newConnection()
		listenChannel = connection.createChannel()
		listenChannel.queueDeclare(taskQueueName, true, false, false, null)
		listenChannel.basicQos(1)
		consumer = new QueueingConsumer(listenChannel)
		listenChannel.basicConsume(taskQueueName, false, consumer)

		sendChannel = connection.createChannel()
		listenChannel.queueDeclare(answerQueueName, false, false, false, null)
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
						send(a)
					case Failure(e) =>
						log.error(e.getStackTraceString)
				}
				listenChannel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
			} else {
				log.warn("Timeouted.")
			}
//			if (i % 10000 == 0)
//				println(i)
		}
	}

	def send(taskAnswer: TaskAnswer): Unit = {
		val compressed = write(taskAnswer).getBytes("UTF-8")
		testUnpack(taskAnswer, compressed)
		sendChannel.basicPublish("", answerQueueName, null, compressed)
	}

	def testUnpack(taskAnswer: TaskAnswer, compressed: Array[Byte]): Unit = {
		try {
			read[TaskAnswer](new String(compressed, "UTF-8"))
		} catch {
			case _: Throwable =>
				println("It failed for")
				println(taskAnswer.header)
				println(taskAnswer.taskId)
				println(taskAnswer.triples)
		}
	}

	def close() {
		sendChannel.close()
		listenChannel.close()
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
