package lod2014group1.amqp

import lod2014group1.rdf.{RdfTripleString, RdfTriple}
import lod2014group1.database.{VirtuosoLocalDatabase, TaskDatabase}
import scala.slick.driver.SQLiteDriver.simple._
import lod2014group1.amqp.worker.{UriFile, TaskAnswer}
import lod2014group1.Config
import java.io.{File, PrintWriter}

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

		writeFiles(answer.files)

		taskDatabase.runInDatabase { tasks => implicit session =>
			val row = tasks.filter(_.id === answer.taskId).map(_.finished)
			row.update(true)
		}
	}

	def writeFiles(files: List[UriFile]) {
		val dir = Config.DATA_FOLDER

		files.foreach { file: UriFile =>
			file.uri match {
				// Freebase
				case uri if uri.startsWith("http://www.freebase.com/)") =>
					val id = uri.split("/").last
					val path = s"$dir/freebase/$id/"
					writeFileContent(file.fileContent, path, "freebase_page.html")

				// TMDB
				case uri if uri.startsWith("http://www.themoviedb.org/movie/)") =>
					val id = uri.split("/").last
					val path = s"$dir/themoviedb/movie/$id/"
					writeFileContent(file.fileContent, path, "tmdb_movie.html")

				case uri if uri.startsWith("http://www.themoviedb.org/person/)") =>
					val id = uri.split("/").last
					val path = s"$dir/themoviedb/person/$id/"
					writeFileContent(file.fileContent, path, "tmdb_person.html")

				// IMDB
				case uri if uri.startsWith("http://www.imdb.com/title/)") =>
					val uriSplit = uri.split("/")
					val id = uriSplit(2).substring(2)
					var filename = uriSplit.last
					if (filename.isEmpty)
						filename = "main.html"
					else
						filename = filename.split("?")(0) + ".html"
					val path = s"$dir/IMDBMovie/$id/"
					writeFileContent(file.fileContent, path, filename)

				case uri if uri.startsWith("http://www.imdb.com/name/)") =>
					val id = uri.split("/").last
					val path = s"$dir/Actor/$id/"
					writeFileContent(file.fileContent, path, "main.html")

				// OFDB
				case uri if uri.startsWith("http://www.ofdb.de/film/)") =>
					val id = uri.split("/").last
					val path = s"$dir/OFDB/$id/"
					writeFileContent(file.fileContent, path, "ofdb_movie.html")

				case _ => println(s"WARNING: Uri not supported!")
			}
		}
	}

	def writeFileContent(content: String, path: String, filename: String) {
		val writer = new PrintWriter(new File(path + filename))
		writer.write(content)
		writer.close()
	}
}
