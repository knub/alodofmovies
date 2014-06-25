package lod2014group1.messaging.worker

import lod2014group1.crawling.{UriToFilename, Crawler}
import java.net.URL
import lod2014group1.triplification.TriplifyDistributor


class CrawlifyWorker {

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val url = params("uri")
		val graph = params("graph")

		val file = UriFile(url, Crawler.downloadFile(new URL(url)), params.getOrElse("flag", ""))

		val fileName = UriToFilename.parse(file)
		val content = file.fileContent

		val triplifier = new TriplifyDistributor
		val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }

		val answerMap: Map[String, String] = Map("graph" -> graph)
		new TaskAnswer(taskId, answerMap, List(file), triples)
	}


}
