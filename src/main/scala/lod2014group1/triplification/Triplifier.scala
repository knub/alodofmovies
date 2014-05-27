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
		else if (f.getName == "main.html")
			new ImdbMainPageTriplifier(imdbId).triplify(f)
		else
			List()
	}
}

object Triplifier extends Logging {
	def go() {
		val triplifier = new Triplifier
		val triples = I.am match {
			case Config.Person.Stefan => {
				triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
			}
			case Config.Person.Tanja => {
				triplifier.triplify(new File("data/IMDBMovie/tt0790636/awards.html"))

				// MAIN PAGE ERROR : tt0000610 tt0000630 tt0000817 tt0000931
				// AWARD PAGE ERROR: tt2024432 tt1798709 tt0790636
			}
			case Config.Person.Rice => {
				new FreebaseFilmsTriplifier("/m/0bdjd").triplify(new File("data/Freebase/film/0bdjd"))
				new FreebaseFilmsTriplifier("/m/0bnwv6").triplify(new File("data/Freebase/film/0bnwv6"))
			}
			case Config.Person.Dominik => {
				new TMDBFilmsTriplifier().triplify(new File("data/TMDBMoviesList/movie/550"))
			}
		}


		triples.foreach(println)
	}
}
