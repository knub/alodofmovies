package lod2014group1.messaging.worker

import java.net.URL

import lod2014group1.Config
import lod2014group1.crawling.{UriToFilename, Crawler}
import lod2014group1.database.Queries
import lod2014group1.merging.MovieMerger
import lod2014group1.triplification.TriplifyDistributor


class CrawlifymergeWorker extends Worker {

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val url = params("uri")
		val graph = params("graph")
		val flag = params("flag")

		val file = UriFile(url, Crawler.downloadFile(new URL(url)), params.getOrElse("flag", ""))

		val fileName = UriToFilename.parse(file)
		val content = file.fileContent

		val triplifier = new TriplifyDistributor
		val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }

    val mergedTriples = MovieMerger.merge(triples)

//		if (flag.equals(Config.DELETE_FIRST_FLAG)) {
//			val id = fileName.split("/")(1)
//			Queries.deleteTriplesForMovie(s"${Config.LOD_PREFIX}Movie$imdbId", graph)
//		}

		val answerMap: Map[String, String] = Map("graph" -> graph)
		new TaskAnswer(taskId, answerMap, List(file), mergedTriples)
	}
}
