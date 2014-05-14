package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis._
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.rdf._
import org.joda.time.DateTime
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.amqp.{Worker, WorkerTask, Recv, Supervisor}

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			Triplifier.go
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
<<<<<<< HEAD
		} else if (args contains "rabbit-worker") {
			Worker.listen()
		} else if (args contains "rabbit-server") {
			val task = WorkerTask("Short Task", 15)
			Supervisor.send(task)
=======
		} else if (args contains "crawl-ofdb") {
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.crawl
>>>>>>> OFDBCrawler updated and ofdbcoverage functionality added
		} else if (args contains "freebase") {
		  val freebase = new FreebaseAPI()
		  //freebase.getAllNotImdbMovies
		  //freebase.getFreebaseFilmsWithIMDB
		  //freebase.getExampleRdf
		  freebase.getAllFilmId
		} else if (args contains "dbpedia") {
			val dbpedia = new DBpediaAPI()
<<<<<<< HEAD
			dbpedia getAllTriplesFor "http://dbpedia.org/resource/Despicable_Me"
=======
			dbpedia.executeQuery("select distinct ?Concept where {[] a ?Concept} LIMIT 100")
		} else if (args contains "ofdb-coverage"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.coverage
>>>>>>> OFDBCrawler updated and ofdbcoverage functionality added
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}
