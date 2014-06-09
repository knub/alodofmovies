package lod2014group1.amqp.worker

import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.apache.commons.io.FileUtils
import java.io.File
import lod2014group1.triplification.Triplifier

class TriplifyWorker extends Worker {

	val triplifier = new Triplifier()

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val tmpFile = File.createTempFile("triplify_tmp", null, null)
		tmpFile.deleteOnExit()
		FileUtils.writeStringToFile(tmpFile, params("content"))
		val triples = triplifier.triplify(tmpFile).map { _.toRdfTripleString() }
		val answerMap: Map[String, String] = Map()

		tmpFile.delete()
		new TaskAnswer(taskId, answerMap, Nil, triples)
	}

}