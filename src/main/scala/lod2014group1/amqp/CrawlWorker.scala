package lod2014group1.amqp

import lod2014group1.FileContent
import lod2014group1.crawling.Crawler
import java.net.{URL, URI}


class CrawlWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val url = params("uri")
		val file = UriFile(url, Crawler.downloadFile(new URL(url)))

		new TaskAnswer(params("task_id") + " downloaded", List(file), Nil)
	}

}
