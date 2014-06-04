package lod2014group1.amqp.worker

import lod2014group1.rdf.{RdfResource, RdfTriple}

class TriplifyWorker extends Worker {

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val name = params("uri").replace("_", " ")
		val s = RdfResource(name)
		val triple = RdfTriple(s, s, s).toRdfTripleString()
		val answerMap: Map[String, String] = Map()

		new TaskAnswer(taskId, answerMap, Nil, List(triple))
	}

}
