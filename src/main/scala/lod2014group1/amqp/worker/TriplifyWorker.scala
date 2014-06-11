package lod2014group1.amqp.worker

import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.apache.commons.io.FileUtils
import java.io.File
import lod2014group1.triplification.Triplifier

class TriplifyWorker extends Worker {

	val triplifier = new Triplifier()

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val fileName = params("fileName")
		val content = params("content")

		val triples = triplifier.triplify(fileName, content).map { _.toRdfTripleString() }
		val answerMap: Map[String, String] = Map()

		new TaskAnswer(taskId, answerMap, Nil, triples)
	}

}
