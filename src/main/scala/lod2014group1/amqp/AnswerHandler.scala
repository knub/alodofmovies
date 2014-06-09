package lod2014group1.amqp

import lod2014group1.rdf.{RdfTripleString, RdfTriple}
import lod2014group1.database.{VirtuosoLocalDatabase, TaskDatabase}
import scala.slick.driver.SQLiteDriver.simple._
import lod2014group1.amqp.worker.{UriFile, TaskAnswer}
import lod2014group1.Config
import java.io.{File, PrintWriter}
import org.slf4s.Logging

class AnswerHandler extends Logging {

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

	def writeFiles(files: List[UriFile]): Unit = {
		val dir = Config.DATA_FOLDER

		files.foreach { file: UriFile =>
			// special flag for freebase, because for freebase we cannot determine whether its a movie
			// or a actor from the url
			val fileName = file.uri match {
				// Freebase
				case uri if uri.startsWith("http://www.freebase.com/)") =>
					val id = uri.split("/").last
					file.flag match {
						case "actor" =>
							s"$dir/freebase//person/$id/freebase_person.html"
						case "movie" =>
							s"$dir/freebase/movie/$id/freebase_movie.html"
						case _ =>
							""
					}

				// TMDB
				case uri if uri.startsWith("http://www.themoviedb.org/movie/)") =>
					val id = uri.split("/").last
					s"$dir/themoviedb/movie/$id/tmdb_movie.html"

				case uri if uri.startsWith("http://www.themoviedb.org/person/)") =>
					val id = uri.split("/").last
					s"$dir/themoviedb/person/$id/tmdb_person.html"

				// IMDB
				case uri if uri.startsWith("http://www.imdb.com/title/)") =>
					val uriSplit = uri.split("/")
					val id = uriSplit(2).substring(2)
					var imdbFileName = uriSplit.last
					if (imdbFileName.isEmpty)
						imdbFileName = "main.html"
					else
						imdbFileName = imdbFileName.split("?")(0) + ".html"
					s"$dir/IMDBMovie/$id/$imdbFileName"

				case uri if uri.startsWith("http://www.imdb.com/name/)") =>
					val id = uri.split("/").last
					s"$dir/Actor/$id/main.html"

				// OFDB
				case uri if uri.startsWith("http://www.ofdb.de/film/)") =>
					val id = uri.split("/").last
					s"$dir/OFDB/$id/ofdb_movie.html"

				case _ =>
					""
			}
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
