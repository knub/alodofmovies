package lod2014group1.crawling

import java.nio.channels.{Channels, ReadableByteChannel}
import java.io.FileOutputStream
import java.net.URL

abstract class Crawler {

	def downloadFile(url: String): Unit = {
		val website = new URL(url)
		val rbc: ReadableByteChannel = Channels.newChannel(website.openStream())
		val fos = new FileOutputStream(website.getFile())
		fos.getChannel().transferFrom(rbc, 0, Long.MaxValue)
	}

	def crawl: Unit
}

object Crawler {
	def crawl: Unit = {
		val crawlers: List[Crawler] = List(new lod2014group1.crawling.IMDBMovieSiteCrawler())

		crawlers.foreach(crawler => crawler.crawl)
	}
}