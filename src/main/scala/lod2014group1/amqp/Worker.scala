package lod2014group1.amqp

import com.rabbitmq.client._
import com.rabbitmq.client.AMQP.BasicProperties
import scala.pickling._
import binary._
import java.util.UUID

class Worker(taskQueueName: String, answerQueueName: String) {
	val connection = ConnectionBuilder.newConnection()
	val channel = connection.createChannel()
	channel.queueDeclare(taskQueueName, true, false, false, null)
	channel.basicQos(1)
	val consumer = new QueueingConsumer(channel)
	channel.basicConsume(taskQueueName, false, consumer)

	val rpcClient = new RPCClient(answerQueueName)

	def listen() {
		println(" [*] Waiting for messages. To exit press CTRL+C")
		while (true) {
			val delivery = consumer.nextDelivery()
			val task = delivery.getBody.unpickle[WorkerTask]
			println(" [x] Received '" + task.msg + "'")
			val answer = execute(task)

			val ack = rpcClient.send(answer)
			if (ack) {
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false)
				println(" [x] Done with '" + task.msg + "'")
			} else {
				println(" [Error] rpc call returned false'")
			}

		}
	}

	def close() {
		rpcClient.close()
		channel.close()
		connection.close()
	}

	def execute(task: WorkerTask): TaskAnswer = {
		Thread.sleep(task.time * 1000)
		new TaskAnswer(task.msg.toLowerCase(), task.msg.toLowerCase().getBytes())
	}
}

class RPCClient(rpcQueueName: String) {
	private val connection = ConnectionBuilder.newConnection()
	private val channel = connection.createChannel()
	private val replyQueueName = channel.queueDeclare().getQueue
	private val consumer = new QueueingConsumer(channel)
	channel.basicConsume(replyQueueName, true, consumer)

	def send(taskAnswer: TaskAnswer): Boolean = {
		val corrId = UUID.randomUUID().toString
		val props = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build()

		channel.basicPublish("", rpcQueueName, props, taskAnswer.pickle.value)

		println(" [x] Waiting for rpc answer")
		var recievedAnswer = false
		while (!recievedAnswer) {
			val delivery = consumer.nextDelivery()
			if (delivery.getProperties.getCorrelationId == corrId) {
				recievedAnswer = delivery.getBody.unpickle[Boolean]
			}
		}
		recievedAnswer
	}

	def close() {
		channel.close()
		connection.close()
	}
}
