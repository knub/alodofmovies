package lod2014group1.amqp.worker

import java.net.URL
import lod2014group1.amqp._
import lod2014group1.crawling.Crawler


class CrawlWorker extends Worker {

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val id = taskId
		val url = params("uri")
		val file = UriFile(url, Crawler.downloadFile(new URL(url)))

		val answerMap: Map[String, String] = Map()
		new TaskAnswer(id, answerMap, List(file), Nil)
	}

}
