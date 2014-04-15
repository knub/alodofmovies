package lod2014group1.crawling

import org.slf4s.Logging
import java.net.URL

class IMDBMoviesListCrawler extends Crawler with Logging {

	def crawl: Unit = {
		log.info("Start.")
		val years = List(2013)
		val  url = new URL("http://www.imdb.com/search/title?at=0&sort=alpha&title_type=feature&year=2013")
		System.out.println(url.getFile);

		val dataManager = new CrawlingDataManager("IMDBMovieSites")

		years.foreach { year =>
			System.out.println(year);
		}
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=51&title_type=feature&year=2013,2013
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=101&title_type=feature&year=2013,2013
//		http://www.imdb.com/search/title?at=0&sort=alpha,asc&start=0&title_type=feature&year=2013,2013
	}
}