package lod2014group1.crawling

import org.apache.http.client.utils.URIBuilder
import java.io.File
import org.apache.commons.io.FileUtils
import lod2014group1.Config
import lod2014group1.Config.Person._
import lod2014group1.I
import scala.collection.JavaConversions._
import org.jsoup.Jsoup

object IMDBMovieCrawler {
	val DOWNLOAD_URL  =  "http://www.imdb.com%s%s"
	val BASE_DIR_NAME = "IMDBMovie"
}

class IMDBMovieCrawler extends Crawler {

	def crawl: Unit = {
		log.info(IMDBMoviesListCrawler.BASE_DIR_NAME)
		val moviesListDir = new File(s"${Config.DATA_FOLDER}/${IMDBMoviesListCrawler.BASE_DIR_NAME}")
		// null means no filtering, true means searching recursively
		val movieLists = FileUtils.listFiles(moviesListDir, null, true).toList.sorted.reverse

		movieLists.foreach { movieList =>
			val doc = Jsoup.parse(movieList, null)
			log.info(movieList.toString)

			doc.select(".title").foreach { el =>
				val movieLink = el.child(1)
				if (movieLink.nodeName() != "a")
					log.error("Found element was not a link.")
				val linkText: String = movieLink.attr("href")
				if (!(linkText matches """/title/tt\d{7}/"""))
					log.error(s"Link '$linkText' does not have the required format.")

				val subPage = I.am match {
					case Dominik => ""
					case Rice    => "xxx"
					case Stefan  => "fullcredits"
					case Tanja   => "xxx"
					case Tim     => "xxx"
				}
				val (_, needsDownloading) = getFile(IMDBMovieCrawler.DOWNLOAD_URL.format(linkText, subPage))
				if (needsDownloading)
					Thread.sleep(getNewRandomWaitingTime())

			}
		}

	}

	def determineFileName(url: URIBuilder): File = {
		val urlSplit = url.getPath.split('/')
		val movieId  = urlSplit(2)
		val pageType = urlSplit(3)
		new File(s"${Config.DATA_FOLDER}/${IMDBMovieCrawler.BASE_DIR_NAME}/$movieId/$pageType.html")
	}
}
