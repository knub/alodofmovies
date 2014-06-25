package lod2014group1.updating

import java.sql.Date

import lod2014group1.Config
import lod2014group1.database.{TaskDatabase, Task}
import lod2014group1.messaging.TaskType
import org.joda.time.DateTime

object ImdbUpdater {
	val COMING_SOON_BASE_URL = "http://www.imdb.com/movies-coming-soon"
	val IMDB_PAGES = List("", "fullcredits", "locations", "keywords", "awards", "releaseinfo")
	val MOVE_BASE_URI = "http://www.imdb.com/title/"
}

class ImdbUpdater {

	def createCrawlifyTasks(movieIds: List[String], flag: String) {
		val date = new Date(new DateTime().toDate.getTime)

		// create crawlify tasks
		val taskList = movieIds.flatMap { id =>
			val url = ImdbUpdater.MOVE_BASE_URI + id + "/"
			ImdbUpdater.IMDB_PAGES.map { page =>
				Task(0, TaskType.Crawlify.toString, date, 10, url + page, false, flag, Config.IMDB_DAILY_GRAPH)
			}
		}

		// add tasks to database
		val database = new TaskDatabase
		database.insertAll(taskList)
	}

}
