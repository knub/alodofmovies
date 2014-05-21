package lod2014group1.amqp

import lod2014group1.rdf.{RdfResource, RdfTriple}

class TriplifyWorker extends Worker {

	def execute(params: Map[String, String]): TaskAnswer = {
		val name = params("uri").replace("_", " ")
		val s = RdfResource(name)
		val triple = RdfTriple(s, s, s)
		new TaskAnswer(params("task_id"), Nil, List(triple))
	}

}
