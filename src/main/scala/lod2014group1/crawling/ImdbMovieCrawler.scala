package lod2014group1.crawling

import org.apache.http.client.utils.URIBuilder
import java.io.File
import org.apache.commons.io.FileUtils
import lod2014group1.Config
import lod2014group1.Config.Person._
import lod2014group1.I
import scala.collection.JavaConversions._
import org.jsoup.Jsoup

object ImdbMovieCrawler {
	val DOWNLOAD_URL  =  "http://www.imdb.com%s%s"
	val BASE_DIR_NAME = "IMDBMovie"
}

class ImdbMovieCrawler extends Crawler {

	def crawl: Unit = {
		log.info(ImdbMoviesListCrawler.BASE_DIR_NAME)
		val moviesListDir = new File(s"${Config.DATA_FOLDER}/${ImdbMoviesListCrawler.BASE_DIR_NAME}")
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
					case Rice    => "awards"
					case Stefan  => "keywords"
					case Tanja   => "keywords"
					case Tim     => "releaseinfo"
				}
				val (_, needsDownloading) = getFile(ImdbMovieCrawler.DOWNLOAD_URL.format(linkText, subPage))
				if (needsDownloading)
					Thread.sleep(getNewRandomWaitingTime())

			}
		}

	}

	def determineFileName(url: URIBuilder): File = {
		val urlSplit = url.getPath.split('/')
		val movieId  = urlSplit(2)
		val pageType = if (urlSplit.length > 3) urlSplit(3) + ".html" else "main.html"
		new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/$movieId/$pageType")
	}
}
