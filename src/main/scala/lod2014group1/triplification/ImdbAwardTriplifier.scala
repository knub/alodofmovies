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

class ImdbAwardTriplifier(val imdbId: String) {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		//val awardPart = doc.getElemntsByTag("h1")

		List()
	}

	def triplifyAwards(table: Elements): List[RdfTriple] = {
		List()
	}

	def handleAward(keyword: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")
		List()
	}

}