package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis._
import lod2014group1.triplification.Triplifier
import lod2014group1.amqp._
import lod2014group1.statistics.FreebaseToImdb

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			Triplifier.go()
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
		} else if (args contains "rabbit-worker") {
			val worker = new WorkReceiver("tasks", "answers")
			worker.listen()
		} else if (args contains "rabbit-server") {
			new Thread(new AmqpMessageListenerThread("answers")).start()
			new Thread(new AmqpMessageListenerThread("answers")).start()
			new Thread(new AmqpMessageListenerThread("answers")).start()
		} else if (args contains "crawl-ofdb") {
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.crawl
		} else if(args contains "freebase-stats"){
			val stat = new FreebaseToImdb
			stat.matchFreebase()
			stat.getStatistic()
		} else if (args contains "freebase") {
		  val freebase = new lod2014group1.crawling.FreebaseFilmCrawler()
		  //freebase.getAllNotImdbMovies
		  //freebase.getFreebaseFilmsWithIMDB
		  //freebase.getExampleRdf
		  //freebase.loadAllFilmId
		  freebase.crawl
		} else if (args contains "dbpedia") {
			val dbpedia = new DBpediaAPI()
			dbpedia getAllTriplesFor "http://dbpedia.org/resource/Despicable_Me"
		} else if (args contains "ofdb-clean"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.clean
		} else if (args contains "ofdb-coverage"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.coverage
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}

object FileContent {
	val longString = scala.io.Source.fromFile("lorem_ipsum", "utf-8").getLines().mkString
}
