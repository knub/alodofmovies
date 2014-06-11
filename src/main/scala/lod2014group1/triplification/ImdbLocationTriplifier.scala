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

	def triplify(content: String): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val doc = Jsoup.parse(content)
		val locationDiv = doc.getElementById("filming_locations_content")

		if (locationDiv == null)
			return List()

		locationDiv.children().foreach(div => {
			if (div.className().equals("soda odd") || div.className().equals("soda even")) {
				triples = triplifyLocations(div) ::: triples
			}
		})

		triples
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
