package lod2014group1.messaging

import lod2014group1.rdf.RdfTripleString
import lod2014group1.database.{VirtuosoLocalDatabase, TaskDatabase}
import scala.slick.driver.MySQLDriver.simple._
import lod2014group1.messaging.worker.{UriFile, TaskAnswer}
import java.io.{File, PrintWriter}
import org.slf4s.Logging
import scala.collection.mutable
import lod2014group1.crawling.UriToFilename

class AnswerHandler extends Logging {

	lazy val BULK_LOAD_SIZE = 100000

	val taskDatabase = new TaskDatabase
	var filesToWrite: List[UriFile] = List()
	val triplesToStore: mutable.Map[String, List[RdfTripleString]] = mutable.Map().withDefaultValue(List[RdfTripleString]())

	val db = new VirtuosoLocalDatabase("http://172.16.22.196:8890/sparql")

	def handleAnswer(answer: TaskAnswer): Unit = {
		val graph = answer.header("graph")

		triplesToStore(graph) = triplesToStore(graph) ::: answer.triples

		if (triplesToStore(graph).size > BULK_LOAD_SIZE) {
			println("Bulk-Loading.")
			db.bulkLoad(triplesToStore(graph), graph)
			triplesToStore(graph) = List()
		}

		writeFiles(answer.files)

		taskDatabase.runInDatabase { tasks => implicit session =>
			val row = tasks.filter(_.id === answer.taskId).map(_.finished)
			row.update(true)
		}
	}

	def writeFiles(files: List[UriFile]): Unit = {
		files.foreach { file: UriFile =>
			val fileName = UriToFilename.parse(file)

			if (fileName != "")
				writeFileContent(file.fileContent, fileName)
			else
				log.error(s"WARNING: URI ${file.uri} not supported!")
		}
	}

	def writeFileContent(content: String, fileName: String) {
		val file = new File(fileName)
		file.getParentFile.mkdirs()
		val writer = new PrintWriter(file)
		writer.write(content)
		writer.close()
	}
}
