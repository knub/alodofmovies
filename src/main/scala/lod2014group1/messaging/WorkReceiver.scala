package lod2014group1.messaging

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import json._
import java.util.UUID
import org.slf4s.{LoggerFactory, Logging}
import scala.util.{Failure, Success, Try}
import TaskType._
import lod2014group1.messaging.worker._
import scala.util.Success
import scala.util.Failure


class WorkReceiver(taskQueueName: String, answerQueueName: String) {

	val log = LoggerFactory.getLogger("TaskAnswerLogger")
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(taskQueueName, true, false, false, null)
	channel.basicQos(1)
	val consumer = new QueueingConsumer(channel)
	channel.basicConsume(taskQueueName, false, consumer)
	val workerAssignment = Map(Triplify -> classOf[TriplifyWorker], Crawl -> classOf[CrawlWorker])

	val rpcClient = new RPCClient(answerQueueName)

	def listen() {
		println("Waiting for messages. To exit press CTRL+C")
		var i = 0
		while (true) {
			i += 1
			val delivery = consumer.nextDelivery()
			val task = new String(Gzipper.uncompress(delivery.getBody), "UTF-8").unpickle[WorkerTask]

			val answer = Try(forwardTask(task))

			answer match {
				case Success(a) =>
					rpcClient.send(a)
					log.info(s"Task finished: ${task.`type`}, id: ${task.taskId}, params: ${task.params.-("content")}}" )
				case Failure(e) =>
					log.error(e.getStackTraceString)
			}
			channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
			if (i % 10000 == 0)
				println(i)
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

	def send(taskAnswer: TaskAnswer): Unit = {
		val corrId = UUID.randomUUID().toString
		val props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build()

		channel.basicPublish("", rpcQueueName, props, Gzipper.compress(taskAnswer.pickle.value.getBytes))

		var receivedAnswer = false
		while (!receivedAnswer) {
			val delivery = consumer.nextDelivery()
			if (delivery.getProperties.getCorrelationId == corrId) {
				receivedAnswer = new String(delivery.getBody, "UTF-8").unpickle[Boolean]
			}
		}
	}

	def close() {
		channel.close()
		connection.close()
	}
}