package lod2014group1.crawling

import org.slf4s.Logging
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.apache.http.client.utils.URIBuilder
import java.io.File
import lod2014group1.Config

object IMDBMoviesListCrawler {
	val DOWNLOAD_URL    =  "http://www.imdb.com/search/title?sort=alpha,asc&start=%s&title_type=feature&year=%2$s,%2$s"
	val BASE_DIR_NAME   = "IMDBMoviesList"
	val MOVIES_PER_LIST = 50
}

class IMDBMoviesListCrawler extends Crawler with Logging {
	def crawl: Unit = {
		log.debug("Start.")
		// first movie ever: 1894
		val years = ((1894 to 2013) diff List(1895, 1896)).reverse


		years.foreach { year =>
			var offset = IMDBMoviesListCrawler.MOVIES_PER_LIST
			val numberOfMovies = downloadFirstFileToGetNumberOfMovies(year)
			log.debug(s"Year: $year, $numberOfMovies movies.")

			while (offset < numberOfMovies) {
				val (_, needsDownloading) = getFile(IMDBMoviesListCrawler.DOWNLOAD_URL.format(offset, year))
				if (needsDownloading)
					Thread.sleep(getNewRandomWaitingTime())
				offset += IMDBMoviesListCrawler.MOVIES_PER_LIST
				log.debug(s"$offset/$numberOfMovies movies.")
			}
		}
	}

	def downloadFirstFileToGetNumberOfMovies(year: Int): Int = {
		val (file, _) = getFile(IMDBMoviesListCrawler.DOWNLOAD_URL.format(0, year))

		// null as second parameter means we just take standard encoding
		val doc = Jsoup.parse(file, null)
		val innerHtml = doc.getElementById("left").html()
		val numberOfMovies = "([\\d,]+) titles".r.findFirstMatchIn(innerHtml).map(_.group(1).replace(",", "").toInt)
		numberOfMovies match {
			case Some(i) => i
			case None => throw new RuntimeException("Could not determine number of movies.")
		}
	}

	def determineFileName(uri: URIBuilder): File = {
		val start = uri.getQueryParams
			.find(_.getName == "start")
			.map(_.getValue.toInt)
			.getOrElse(throw new RuntimeException("Could not find start param."))

		val year = uri.getQueryParams
			.find(_.getName == "year")
			.map(_.getValue.substring(0, 4).toInt)
			.getOrElse(throw new RuntimeException("Could not find start param."))

		val prependedStart = "%04d".format(start)
		new File(s"${Config.DATA_FOLDER}/${IMDBMoviesListCrawler.BASE_DIR_NAME}/$year/IMDBMovies$prependedStart.html")
	}
}
