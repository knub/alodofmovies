package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.Config.Person
import lod2014group1.triplification.Triplifier
import java.io.File

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		if (I.am == Person.Stefan) {
			val triplifier = new Triplifier
			triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
		} else if (I.am == Person.Tanja) {
    		val triplifier = new Triplifier
     		//triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html"))
    		triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
    	} else if (I.am == Person.Dominik) {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
    	} else {
			Crawler.crawl
		}
		log.debug("Finished.")
	}
}
