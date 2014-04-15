package lod2014group1

import org.joda.time.DateTime
import lod2014group1.crawling.Crawler

object Main extends App {

	override def main(args: Array[String]): Unit = {
		Crawler.crawl;
	}
}
