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

class ImdbKeywordTriplifier(val imdbId: String) {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		val keywordsDiv = doc.getElementById("keywords_content")
		if (keywordsDiv == null) {
			List()
		} else {
			val keywordsTable = keywordsDiv.select("table")

			triplifyKeywords(keywordsTable)
		}
	}

	def triplifyKeywords(table: Elements): List[RdfTriple] = {
		table.select("tr td a").toList.flatMap { keyword =>
			handleKeyword(keyword.ownText())
		}
	}

	def handleKeyword(keyword: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")

		List(movie hasKeyword keyword)
	}

}