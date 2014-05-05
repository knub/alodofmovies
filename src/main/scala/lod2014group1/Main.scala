package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis.FreebaseAPI
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.rdf.RdfResource
import org.joda.time.DateTime
import lod2014group1.rdf.RdfMovieResource.fromRdfResource

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			val triplifier = new Triplifier
			triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
//			triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
			//triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html"))
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
		} else if (args contains "freebase") {
		  val freebase = new FreebaseAPI()
		  freebase.getAllNotImdbMovies
		  //freebase.getExampleRdf
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
			val forrestGump = new RdfResource("http://dbpedia.org/resource/Forrest_Gump")
//			val rdfTriple = forrestGump releasedOn new DateTime(1994, 7, 6, 0, 0, 0)
			val rdfTriple = forrestGump releasedOn new DateTime(1994, 7, 6, 0, 0, 0)
			System.out.println(rdfTriple);
		}
		log.debug("Finished.")
	}
}
