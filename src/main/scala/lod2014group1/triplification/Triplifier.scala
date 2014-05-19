package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import lod2014group1.{Config, I}
import org.apache.commons.io.FileUtils
import lod2014group1.crawling.ImdbMovieCrawler
import scala.collection.JavaConversions._
import org.slf4s.Logging

class Triplifier {

	def triplify(f: File): List[RdfTriple] = {
		val imdbId = f.getPath().split("/")(2).replaceAll("\\D", "")

		if (f.getName == "fullcredits.html")
			new ImdbCastTriplifier(imdbId).triplify(f)
		else if (f.getName == "locations.html")
			new ImdbLocationTriplifier(imdbId).triplify(f)
		else if (f.getName == "keywords.html")
			new ImdbKeywordTriplifier(imdbId).triplify(f)
		else if (f.getName == "awards.html")
			new ImdbAwardTriplifier(imdbId).triplify(f)
		else if (f.getName == "releaseinfo.html")
			new ImdbReleaseInfoTriplifier(imdbId).triplify(f)
		else if (f.getName.startsWith("tt"))
			new ImdbMainPageTriplifier(imdbId).triplify(f)
		else
			List()
	}
}

object Triplifier extends Logging {
	def go() {
		val triplifier = new Triplifier
		val movieDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")
		log.info("Started grabbing files.");
		val movieFiles = FileUtils.listFiles(movieDir, null, true).toList.sorted.reverse
		log.info("Found " + movieFiles.size + " movies.")
		movieFiles.take(10).foreach(println)
		val triples = I.am match {
			case Config.Person.Stefan => {
				triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
			}
			case Config.Person.Tanja => {
/*				triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html")) :::
				triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html")) :::
				triplifier.triplify(new File("data/IMDBMovie/tt0758758/awards.html")) :::
				triplifier.triplify(new File("data/IMDBMovie/tt0050900/releaseinfo.html")) :::*/
					triplifier.triplify(new File("data/IMDBMovie/tt0137523/tt0137523.html"))
			}
			case Config.Person.Rice => {
				new FreebaseFilmsTriplifier("/m/0_7w6").triplify(new File("data/Freebase/0_7w6"))
			}
			case Config.Person.Dominik => {
				new TMDBFilmsTriplifier().triplify(new File("data/TMDBMoviesList/movie/550"))
			}
		}


		triples.foreach(println)
	}
}
