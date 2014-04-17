package lod2014group1.crawling

import org.slf4s.Logging
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.apache.http.client.utils.URIBuilder
import java.io.File

class IMDBMoviesListCrawler extends Crawler with Logging {
	val DOWNLOAD_URL    =  "http://www.imdb.com/search/title?sort=alpha,asc&start=%s&title_type=feature&year=%2$s,%2$s"
	val MOVIES_PER_LIST = 50

	def crawl: Unit = {
		log.info("Start.")
		val years = List(2012)


		var offset = 0;
		years.foreach { year =>
			val numberOfMovies = downloadFirstFileToGetNumberOfMovies(year);
			System.out.println(numberOfMovies);

			while (offset < numberOfMovies) {
				Thread.sleep(getNewRandomWaitingTime())
				val file = getFile(DOWNLOAD_URL.format(offset, year))
				offset += MOVIES_PER_LIST
				System.out.println(s"$offset/$numberOfMovies");
			}
		}
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=101&title_type=feature&year=2013,2013
	}

	def downloadFirstFileToGetNumberOfMovies(year: Int): Int = {
		val file = getFile(DOWNLOAD_URL.format(0, year))

		val doc = Jsoup.parse(file, null)
		val innerHtml = doc.getElementById("left").html()
		val numberOfMovies = "of ([\\d,]+) titles".r.findFirstMatchIn(innerHtml).map(_.group(1).replace(",", "").toInt)
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

		new File(s"data/IMDBMoviesList/$year/IMDBMovies$start.html")
	}
}