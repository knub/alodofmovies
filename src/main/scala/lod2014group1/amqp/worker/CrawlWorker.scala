package lod2014group1.amqp.worker

import java.net.URL
import lod2014group1.amqp._
import lod2014group1.crawling.Crawler


class CrawlWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val id = params("task_id")
		val url = params("uri")
		val file = UriFile(url, Crawler.downloadFile(new URL(url)))

		val answerMap = Map("task_id" -> id)
		new TaskAnswer(answerMap, List(file), Nil)
	}

}
