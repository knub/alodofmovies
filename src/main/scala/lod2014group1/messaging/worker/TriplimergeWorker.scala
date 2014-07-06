package lod2014group1.messaging.worker

import java.net.URL

import lod2014group1.Config
import lod2014group1.crawling.{UriToFilename, Crawler}
import lod2014group1.database.Queries
import lod2014group1.merging.MovieMerger
import lod2014group1.triplification.TriplifyDistributor


class TriplimergeWorker extends Worker{

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val fileName = params("fileName")
		val content = params("content")
		val graph = params("graph")

		val triplifier = new TriplifyDistributor
		val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }

		val mergedTriples = MovieMerger.merge(triples)

    if (mergedTriples.isEmpty) {
      // create new match task

    }

		val answerMap: Map[String, String] = Map("graph" -> graph)
		new TaskAnswer(taskId, answerMap, Nil, mergedTriples)
	}
}
