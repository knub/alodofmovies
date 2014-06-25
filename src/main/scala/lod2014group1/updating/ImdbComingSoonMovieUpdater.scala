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

object ImdbComingSoonMovieUpdater {
	val BASEURL = "http://www.imdb.com/movies-coming-soon"
}

class ImdbComingSoonMovieUpdater() {

	def createCralifyTaskForNewMovies(): Unit = {
		val contents = crawlComingSoonPage()
		val ids = contents.flatMap{ content =>
			getNewMovieIds(content)
		}
		addCrawlifyTasksFor(ids)
	}

	private def addCrawlifyTasksFor(ids: List[String]): Unit = {
		val movieBaseUrl = "http://www.imdb.com/title/tt"

		// task should be done the following day
		val dt = new DateTime()
		val date = new Date(dt.toDate.getTime)

		// create crawlify tasks
		val taskList = ids.map { id =>
			Task(0, TaskType.Crawlify.toString, date, 10, movieBaseUrl + id, false, "", Config.IMDB_GRAPH)
		}

		// add tasks to database
		val database = new TaskDatabase
		database.insertAll(taskList)
	}

	private def getNewMovieIds(content: String): List[String] = {
		val doc = Jsoup.parse(content)
		val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

		var ids: List[String] = List()
		movieIdTags.foreach { movieId: Element =>
			ids = movieId.attr("href").split("/")(2).substring(2) :: ids
		}
		ids
	}

	private def crawlComingSoonPage(): List[String] = {
		var dates: List[String] = List()
		var currentDate = new DateTime()

		(1 to 12).foreach { i =>
			val year = currentDate.getYear
			val month = currentDate.getMonthOfYear
			val dateStr = "%s-%02d".format(year, month)
			dates = dateStr :: dates
			currentDate = currentDate.plusMonths(1)
		}

		dates.flatMap { date =>
			val url = s"${ImdbComingSoonMovieUpdater.BASEURL}/$date"
			List(Crawler.downloadFile(new URL(url)))
		}
	}

}
