package lod2014group1

import org.joda.time.DateTime
import lod2014group1.crawling.Crawler
import java.io.File
import org.slf4s.Logging

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.info("Started.")
		Crawler.crawl;
		log.info("Finished.")
	}
}
