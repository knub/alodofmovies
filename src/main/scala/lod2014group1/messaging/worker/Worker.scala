package lod2014group1.messaging.worker

abstract class Worker {

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer
}
