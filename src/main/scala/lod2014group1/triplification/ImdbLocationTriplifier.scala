package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.RdfTriple
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._

class ImdbLocationTriplifier {
	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)
		val locationDiv = doc.getElementById("filming_locations_content")

		locationDiv.children().foreach(div => {
			if (div.className().equals("soda odd") || div.className().equals("soda even")) {
				triplifyLocations(div)
			}
		})
		List()
	}

	def triplifyLocations(location: Element): List[RdfTriple] = {
		location.select("dt a").foreach( loc => {
			System.out.println("location: " + loc.ownText())
			System.out.println("url: " + loc.attr("href"))
		})
		List()
	}
}
