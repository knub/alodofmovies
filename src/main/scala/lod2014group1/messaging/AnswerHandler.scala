package lod2014group1.messaging

import lod2014group1.rdf.{RdfTripleString, RdfTriple}
import lod2014group1.database.{VirtuosoLocalDatabase, TaskDatabase}
import scala.slick.driver.SQLiteDriver.simple._
import lod2014group1.messaging.worker.{UriFile, TaskAnswer}
import lod2014group1.Config
import java.io.{File, PrintWriter}
import org.slf4s.Logging
import lod2014group1.crawling.UriToFilename

class AnswerHandler extends Logging {

	lazy val BULK_LOAD_SIZE = 1000000

	val taskDatabase = new TaskDatabase
	var filesToWrite: List[UriFile] = List()
	var triplesToStore: List[RdfTripleString] = List()

	val db = new VirtuosoLocalDatabase("http://172.16.22.196:8890/sparql")

	def handleAnswer(answer: TaskAnswer): Unit = {
		triplesToStore = triplesToStore ::: answer.triples

		println(triplesToStore.size)
		if (triplesToStore.size > BULK_LOAD_SIZE) {
			db.bulkLoad(triplesToStore, "http://hpi.uni-potsdam.de/lod2014group1-test")
			triplesToStore = List()
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
