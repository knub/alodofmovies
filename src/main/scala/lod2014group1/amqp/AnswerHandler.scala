package lod2014group1.amqp

import lod2014group1.rdf.{RdfTripleString, RdfTriple}

class AnswerHandler {

	var filesToWrite: List[UriFile] = List()
	var triplesToStore: List[RdfTripleString] = List()
	def handleAnswer(answer: TaskAnswer): Unit = {
		triplesToStore = triplesToStore ::: answer.triples
		filesToWrite

	}

}
