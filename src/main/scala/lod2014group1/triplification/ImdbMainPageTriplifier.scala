package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.{RdfResource, RdfTriple}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class ImdbMainPageTriplifier(val imdbId: String) {

	val movie = RdfResource(s"lod:Movie$imdbId")

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		var triples: List[RdfTriple] = List()

		val overviewDiv = doc.select(".article.title-overview")
		triples = handleOverviewDiv(overviewDiv) ::: triples

		triples
	}

	def handleOverviewDiv(div: Elements): List[RdfTriple] = {
		val poster = div.select(".image img").attr("src")
		val title = div.select(".header [itemprop=name]").text();
		val year = div.select(".header .nobr a").text();
		val runtime = div.select(".infobar [itemprop=duration]").text();
		val genres = div.select((".infobar [itemprop=genre]"))


		List()
	}

}