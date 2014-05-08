package lod2014group1.crawling

import org.apache.http.client.utils.URIBuilder
import scala.collection.JavaConversions._
import java.io.File
import lod2014group1.Config
import lod2014group1.triplification.ImdbCastTriplifier
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.io.filefilter.{NameFileFilter, TrueFileFilter}

object ImdbActorCrawler {
	val DOWNLOAD_URL  = "http://www.imdb.com%s"
	val BASE_DIR_NAME = "Actor"
}
class ImdbActorCrawler extends Crawler {

	val castParser = new ImdbCastTriplifier

	def crawl: Unit = {
		val moviesDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")

		var movieCount = 0
		val movieFiles = moviesDir.listFiles
		val movieNumber = movieFiles.size
		movieFiles.foreach { f =>
			val actorUrls = castParser.getActorUrls(new File(f, "fullcredits.html"))
			actorUrls.take(5).foreach { actorUrl =>
				getFile(ImdbActorCrawler.DOWNLOAD_URL.format(actorUrl))
			}
			movieCount += 1
			if (movieCount % 100 == 0)
				log.info(s"$movieCount/$movieNumber");
		}
	}

	def determineFileName(url: URIBuilder): File = {
		val urlSplit = url.getPath.split('/')
		val nameId  = urlSplit(2)
		new File(s"${Config.DATA_FOLDER}/${ImdbActorCrawler.BASE_DIR_NAME}/$nameId/main.html")
	}

}
