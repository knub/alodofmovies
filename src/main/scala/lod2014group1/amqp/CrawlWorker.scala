package lod2014group1.amqp

import lod2014group1.FileContent


class CrawlWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val file = UriFile(params("uri"), FileContent.longString)

		new TaskAnswer(params("task_id"), List(file), Nil)
	}

}
