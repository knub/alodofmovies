package lod2014group1.amqp

import lod2014group1.rdf.{RdfTripleString, RdfTriple}
import lod2014group1.database.{VirtuosoLocalDatabase, TaskDatabase}
import scala.slick.driver.SQLiteDriver.simple._
import lod2014group1.amqp.worker.{UriFile, TaskAnswer}

class AnswerHandler {

	lazy val BULK_LOAD_SIZE = 100

	val taskDatabase = new TaskDatabase
	var filesToWrite: List[UriFile] = List()
	var triplesToStore: List[RdfTripleString] = List()

	val db = new VirtuosoLocalDatabase("http://172.16.22.196:8890/sparql")

	def handleAnswer(answer: TaskAnswer): Unit = {
		triplesToStore = triplesToStore ::: answer.triples
		if (triplesToStore.size > BULK_LOAD_SIZE) {
			db.bulkLoad(triplesToStore, "http://hpi.uni-potsdam.de/lod2014group1-test")
			triplesToStore = List()
		}
//		filesToWrite
		taskDatabase.runInDatabase { tasks => implicit session =>
			val row = tasks.filter(_.id === answer.taskId).map(_.finished)
			row.update(true)
		}
	}
}
