package lod2014group1.amqp


class CrawlWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val file = UriFile(params("uri"), params("uri").replace("_", " ").replaceFirst("http://", ""))

		new TaskAnswer(params("task_id"), List(file), Nil)
	}

}
