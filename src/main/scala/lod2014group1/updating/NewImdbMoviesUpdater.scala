package lod2014group1.updating

import java.io.File
import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import lod2014group1.database.{Task, TaskDatabase}
import org.joda.time.{Days, DateTime}
import java.sql.Date
import lod2014group1.amqp.TaskType

class NewImdbMoviesUpdater() {

	def getNewMovieIds(f: File) {
		var ids: List[String] = List()

		val doc = Jsoup.parse(f, null)
		val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

		movieIdTags.foreach { movieId: Element =>
			ids = movieId.attr("href").split("/")(2).substring(2) :: ids
		}

		createTriplifyTasks(ids)
	}

	def createTriplifyTasks(ids: List[String]) {
		val baseUri = "http://www.imdb.com/title/tt"
		val dt = new DateTime
		dt.plusDays(1)
		val date = new Date(dt.toDate.getTime)

		val taskList = ids.map { id =>
			Task(0, TaskType.Crawlify.toString, date, 10, baseUri + id, false, "")
		}

		val database = new TaskDatabase
		database.insertAll(taskList)

	}
}
