package lod2014group1.crawling

import org.slf4s.Logging
import java.net.URL

class IMDBMoviesListCrawler extends Crawler with Logging {

	val moviesPerList: Int = 50

	def crawl: Unit = {
		log.info("Start.")
		val years = List(2013)

		val dataManager = new CrawlingDataManager("IMDBMoviesList")

		var offset = 0;
		years.foreach { year =>
			val channel = downloadFile(s"http://www.imdb.com/search/title?sort=alpha,asc&start=$offset&title_type=feature&year=2013,2013")
			val fileName = dataManager.saveFile(channel, year.toString, s"start${offset.toString}.html")
			//			val  url = new URL()
//			System.out.println(year);
		}
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=101&title_type=feature&year=2013,2013
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=0&title_type=feature&year=2013,2013
	}
}