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

class ImdbReleaseInfoTriplifier(val imdbId: String) {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		List()
	}

	def triplifyReleaseInfo(table: Elements): List[RdfTriple] = {
		List()
	}

	def handleReleaseInfo(keyword: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")

		List()
	}

}