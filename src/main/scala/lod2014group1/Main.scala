package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
<<<<<<< HEAD
import lod2014group1.apis.FreebaseAPI
=======
import lod2014group1.Config.Person
import lod2014group1.triplification.Triplifier
import java.io.File
import scala.collection.JavaConversions._
>>>>>>> 21c420d6723b30e7e7f64a673df37515bb1fd916

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			val triplifier = new Triplifier
			triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
			triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
			//triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html"))
		} else if (args contains "crawl") {
			Crawler.crawl
		} else if (args contains "freebase") {
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}
