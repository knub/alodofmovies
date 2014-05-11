package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis._
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.rdf._
import org.joda.time.DateTime
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.amqp.{WorkerTask, Recv, Supervisor}

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
		} else if (args contains "rabbit") {
			val task = WorkerTask("Short Task", 15)
			Supervisor.send(task)
			Recv.listen()
		} else if (args contains "freebase") {
		  val freebase = new FreebaseAPI()
		  //freebase.getAllNotImdbMovies
		  //freebase.getFreebaseFilmsWithIMDB
		  //freebase.getExampleRdf
		  freebase.getAllFilmId
		} else if (args contains "dbpedia") {
			val dbpedia = new DBpediaAPI()
			dbpedia getAllTriplesFor "http://dbpedia.org/resource/Despicable_Me"
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
			val forrestGump = new RdfResource("http://dbpedia.org/resource/Forrest_Gump")
//			val rdfTriple = forrestGump releasedOn new DateTime(1994, 7, 6, 0, 0, 0)
			val rdfTriple1 = forrestGump releasedOn new DateTime(1994, 7, 6, 0, 0, 0)
			val rdfTriple2 = forrestGump isA film
			System.out.println(rdfTriple1);
			System.out.println(rdfTriple2);
		}
		log.debug("Finished.")
	}
}
