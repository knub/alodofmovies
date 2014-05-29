package lod2014group1.amqp.worker

import lod2014group1.rdf.{RdfResource, RdfTriple}

class TriplifyWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val id = params("task_id")
		val name = params("uri").replace("_", " ")
		val s = RdfResource(name)
		val triple = RdfTriple(s, s, s).toRdfTripleString()
		val answerMap = Map("task_id" -> id)
		new TaskAnswer(answerMap, Nil, List(triple))
	}

}
