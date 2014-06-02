package lod2014group1.amqp.worker

abstract class Worker {

	def execute(params: Map[String, String]): TaskAnswer
}
