package lod2014group1.updating

import java.io.{FileWriter, PrintWriter, File}
import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import lod2014group1.database.{Task, TaskDatabase}
import org.joda.time.{Days, DateTime}
import java.sql.Date
import lod2014group1.messaging.TaskType
import lod2014group1.Config
import lod2014group1.crawling.Crawler
import java.net.URL
import org.joda.time.format.DateTimeFormat

object NewImdbMoviesUpdater {
	def watchUpcomingMovies() {
		val baseUrl = "http://www.imdb.com/movies-coming-soon"
		val path = "/home/tanja/Desktop/IMDB"

		val ids = (6 to 12).flatMap { currMonth: Int =>
			val m = "%02d" format currMonth
			val url = s"$baseUrl/2014-$m"
			val content = Crawler.downloadFile(new URL(url))
			getIds(content)
		}

		val format = DateTimeFormat.forPattern("y-MM-dd")
		val dateStr = format.print(new DateTime())

		val writer = new PrintWriter(new File(s"$path/$dateStr"))
		ids.sorted.foreach(writer.println)
		writer.close()

	}

	def getIds(content: String): List[String] = {
		var ids: List[String] = List()

		val doc = Jsoup.parse(content)
		val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

		movieIdTags.foreach { movieId: Element =>
			ids = movieId.attr("href").split("/")(2).substring(2) :: ids
		}

		ids
	}
}

class NewImdbMoviesUpdater() {

	def getNewMovieIds(content: String): Unit = {
		var ids: List[String] = List()

		val doc = Jsoup.parse(content)
		val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

		movieIdTags.foreach { movieId: Element =>
			ids = movieId.attr("href").split("/")(2).substring(2) :: ids
		}

		createTriplifyTasks(ids)
	}

	def createTriplifyTasks(ids: List[String]): Unit = {
		val baseUri = "http://www.imdb.com/title/tt"
		val dt = new DateTime
		dt.plusDays(1)
		val date = new Date(dt.toDate.getTime)

		val taskList = ids.map { id =>
			Task(0, TaskType.Crawlify.toString, date, 10, baseUri + id, 0, "", Config.IMDB_GRAPH)
		}

		val database = new TaskDatabase
		database.insertAll(taskList)
	}

}
