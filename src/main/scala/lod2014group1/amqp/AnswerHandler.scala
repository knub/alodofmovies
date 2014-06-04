package lod2014group1.amqp

import lod2014group1.rdf.{RdfTripleString, RdfTriple}
import lod2014group1.database.TaskDatabase
import scala.slick.driver.SQLiteDriver.simple._
import lod2014group1.amqp.worker.{UriFile, TaskAnswer}

class AnswerHandler {

	val taskDatabase = new TaskDatabase
	var filesToWrite: List[UriFile] = List()
	var triplesToStore: List[RdfTripleString] = List()

	def handleAnswer(answer: TaskAnswer): Unit = {
		triplesToStore = triplesToStore ::: answer.triples
		filesToWrite
		taskDatabase.runInDatabase { tasks => implicit session =>
			val row = tasks.filter(_.id === answer.header("task_id").toInt).map(_.finished)
			row.update(true)
		}
	}
}
