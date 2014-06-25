package lod2014group1.messaging.worker

import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.apache.commons.io.FileUtils
import java.io.File
import lod2014group1.triplification.TriplifyDistributor

class TriplifyWorker extends Worker {

	val triplifier = new TriplifyDistributor()

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val fileName = params("fileName")
		val content = params("content")
		val graph = params("graph")

		val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }
		val answerMap: Map[String, String] = Map("graph" -> graph)

		new TaskAnswer(taskId, answerMap, Nil, triples)
	}

}
