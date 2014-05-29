package lod2014group1.amqp

import lod2014group1.crawling.Crawler
import java.net.URL


class CrawlWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val id = params("task_id")
		val url = params("uri")
		val file = UriFile(url, Crawler.downloadFile(new URL(url)))

		val answerMap = Map("task_id" -> id)
		new TaskAnswer(answerMap, List(file), Nil)
	}

}
