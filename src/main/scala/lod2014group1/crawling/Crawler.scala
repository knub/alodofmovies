package lod2014group1.crawling

import java.net.{URLConnection, URL}
import org.slf4s.Logging
import java.nio.channels._

abstract class Crawler {

	def downloadFile(url: String): ReadableByteChannel = {
		val website = new URL(url)
		val connection = website.openConnection()
		connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//		connection.addRequestProperty("Accept-Encoding", "gzip,deflate,sdch")
		connection.addRequestProperty("Accept-Language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4")
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36")
		val inputStream = connection.getInputStream
		Channels.newChannel(inputStream)
	}

	def crawl: Unit
}

object Crawler extends Logging {
	def crawl: Unit = {
		val crawlers: List[Crawler] = List(new lod2014group1.crawling.IMDBMoviesListCrawler())

		log.info("Start crawling.")
		crawlers.foreach(crawler => crawler.crawl)
	}
}