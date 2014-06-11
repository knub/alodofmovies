package lod2014group1.messaging

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import json._
import org.slf4s._
import org.slf4s.Logger
import lod2014group1.messaging.worker.{WorkerTask, TaskAnswer}

class TaskDistributor() extends Logging {
	val taskQueueName = "tasks"
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(taskQueueName, true, false, false, null)

	def send(task: WorkerTask) {
		channel.basicPublish("", taskQueueName, MessageProperties.PERSISTENT_TEXT_PLAIN, Gzipper.compress(task.pickle.value.getBytes("UTF-8")))
		log.debug(s"[x] Sent '${task.`type`}' to queue")
	}

	def close() {
		channel.close()
		connection.close()
	}
}

class AmqpMessageListenerThread(rpcQueueName: String) extends Runnable {
	val log = LoggerFactory.getLogger("TaskAnswerLogger")
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(rpcQueueName, false, false, false, null)
	channel.basicQos(1)
	val consumer = new QueueingConsumer(channel)
	channel.basicConsume(rpcQueueName, false, consumer)

	// domain-specific variables
	var answersReceived = 0
	val answersLog: Logger = LoggerFactory.getLogger("TaskAnswerLogger")
	val answerHandler = new AnswerHandler()

	override def run(): Unit = {
		println("Awaiting RPC requests")
		while (true) {
			val delivery = consumer.nextDelivery()

			handle(delivery.getBody)

			val props = delivery.getProperties
			val replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId).build()
			channel.basicPublish("", props.getReplyTo, replyProps, true.pickle.value.getBytes)
			channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
		}
	}

	def close() {
		channel.close()
		connection.close()
	}

	def handle(messageBody: Array[Byte]): Unit = {
		val answer = new String(Gzipper.uncompress(messageBody), "UTF-8").unpickle[TaskAnswer]
		answersReceived += 1
		answerHandler.handleAnswer(answer)

		answersLog.info(s"Received Task-ID: ${answer.taskId}, Header: ${answer.header}")
		answersLog.info(s"Saved ${answer.files.size} files.")
		answersLog.info(s"Stored ${answer.triples.size} triples.")
	}
}

