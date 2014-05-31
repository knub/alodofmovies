package lod2014group1.crawling

import org.apache.http.client.utils.URIBuilder
import scala.collection.JavaConversions._
import java.io.File
import lod2014group1.Config
import lod2014group1.triplification.ImdbCastTriplifier
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.io.filefilter.{NameFileFilter, TrueFileFilter}
import lod2014group1.database.{Task, TaskDatabase}
import java.sql.Date

object ImdbActorCrawler {
	val DOWNLOAD_URL  = "http://www.imdb.com%s"
	val BASE_DIR_NAME = "Actor"
}
class ImdbActorCrawler extends Crawler {

	val castParser = new ImdbCastTriplifier("fakeid")
	val taskDatabase = new TaskDatabase

	def crawl: Unit = {
		val moviesDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")

		var movieCount = 0
		val movieFiles = moviesDir.listFiles.sorted
		val movieNumber = movieFiles.size
		var tasks: List[Task] = List()
		movieFiles.drop(movieCount).foreach { f =>
			val actorUrls = castParser.getActorUrls(new File(f, "fullcredits.html"))
			actorUrls.take(5).foreach { actorUrl =>
				try {
					getFile(actorUrl)
				} catch {
					case e: java.io.FileNotFoundException =>
				}
			}
//			val tmpTasks = actorUrls.take(5).map { actorUrl =>
//				Task(0, "download", new Date(2014, 6, 10), 5, actorUrl, finished)
//			}
//			tasks = tasks ::: tmpTasks
//			if (tasks.size > 10000) {
//				taskDatabase.insertAll(tasks: _*)
//				tasks = List()
//			}
			movieCount += 1
			if (movieCount % 100 == 0)
				log.info(s"$movieCount/$movieNumber");
		}
	}

	def determineFileName(url: URIBuilder): File = {
		val urlSplit = url.getPath.split('/')
		val nameId  = urlSplit(2)
		val f = new File(s"${Config.DATA_FOLDER}/${ImdbActorCrawler.BASE_DIR_NAME}/$nameId/main.html")
		f
	}

}
