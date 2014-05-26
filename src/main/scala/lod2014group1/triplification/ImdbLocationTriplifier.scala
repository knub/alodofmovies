package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.RdfTriple
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfMovieResource._

class ImdbLocationTriplifier(val imdbId: String) {

	def triplify(f: File): List[RdfTriple] = {
		try {
			var triples: List[RdfTriple] = List()

			val doc = Jsoup.parse(f, null)
			val locationDiv = doc.getElementById("filming_locations_content")

			locationDiv.children().foreach(div => {
				if (div.className().equals("soda odd") || div.className().equals("soda even")) {
					triples = triplifyLocations(div) ::: triples
				}
			})

			triples
		} catch {
			case e: Exception =>
				println(s"File $f empty.")
				List()
		}
	}

	def triplifyLocations(location: Element): List[RdfTriple] = {
		location.select("dt a").toList.flatMap { loc =>
			handleLocation(loc.ownText())
		}
	}

	def handleLocation(location: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")

		List(movie filmedInLocation location)
	}
}
