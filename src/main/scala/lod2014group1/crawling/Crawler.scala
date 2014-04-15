package lod2014group1.crawling

import java.nio.channels.{Channels, ReadableByteChannel}
import java.io.FileOutputStream
import java.net.URL
import org.slf4s.Logging

abstract class Crawler {

	def downloadFile(url: String): Unit = {
		val website = new URL(url)
		val rbc: ReadableByteChannel = Channels.newChannel(website.openStream())
		val fos = new FileOutputStream(website.getFile())
		fos.getChannel().transferFrom(rbc, 0, Long.MaxValue)
	}

	def crawl: Unit
}

object Crawler extends Logging {
	def crawl: Unit = {
		val crawlers: List[Crawler] = List(new lod2014group1.crawling.IMDBMovieSiteCrawler())

		log.info("Start crawling.")
		crawlers.foreach(crawler => crawler.crawl)
	}
}