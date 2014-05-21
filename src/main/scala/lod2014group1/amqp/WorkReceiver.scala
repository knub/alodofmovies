package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import java.util.UUID
import org.slf4s.Logging
import scala.util.{Failure, Success, Try}
import TaskType._


class WorkReceiver(taskQueueName: String, answerQueueName: String) extends Logging {
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(taskQueueName, true, false, false, null)
	channel.basicQos(1)
	val consumer = new QueueingConsumer(channel)
	channel.basicConsume(taskQueueName, false, consumer)
	val workerAssignment = Map(Triplify -> classOf[TriplifyWorker], Crawl -> classOf[CrawlWorker])

	val rpcClient = new RPCClient(answerQueueName)

	def listen() {
		println(" [*] Waiting for messages. To exit press CTRL+C")
		while (true) {
			val delivery = consumer.nextDelivery()
			val task = delivery.getBody.unpickle[WorkerTask]
			log.info(" [x] Received '" + task.`type` + "'")

			val answer = Try(forwardTask(task))

			answer match {
				case Success(a) => {
					rpcClient.send(a)
					log.info(" [x] Done with '" + task.`type` + "'")
				}
				case Failure(e) => {
					log.error(e.toString)
				}
			}
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false)
		}
	}

	def close() {
		rpcClient.close()
		channel.close()
		connection.close()
	}

	def forwardTask(task: WorkerTask): TaskAnswer = {
		val worker: Worker = workerAssignment.getOrElse(task.`type`, classOf[DummyWorker]).newInstance()

		worker.execute(task.params)
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

		channel.basicPublish("", rpcQueueName, props, taskAnswer.pickle.value)

		log.info(" [x] Waiting for rpc answer")
		var receivedAnswer = false
		while (!receivedAnswer) {
			val delivery = consumer.nextDelivery()
			if (delivery.getProperties.getCorrelationId == corrId) {
				receivedAnswer = delivery.getBody.unpickle[Boolean]
			}
		}
	}

	def close() {
		channel.close()
		connection.close()
	}
}
