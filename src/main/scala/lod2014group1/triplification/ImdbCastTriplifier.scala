package lod2014group1.triplification

import lod2014group1.rdf._
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.{Document, Element}
import lod2014group1.rdf.RdfActorResource._
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource

class ImdbCastTriplifier {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)
		val table = getContentTable(doc)
		triplifyCast(table)
	}

	def handleCast[A](contentTable: Element, handler: (Element) => List[A]) = {
		val tables = contentTable.select("#fullcredits_content")
		val castTable = tables.select("table.cast_list")
		if (castTable.size() > 1)
			throw new RuntimeException("More than one cast list!")
		if (castTable.isEmpty)
			List()
		else
			castTable.select("td a span").toList.flatMap(handler)
	}

	def triplifyCast(castTable: Element): List[RdfTriple] = {
		handleCast(castTable, extractRdfTriples)
	}

	def getActorUrls(f: File): List[String] = {
		getActorUrls(Jsoup.parse(f, null))
	}

	private def getActorUrls(doc: Document): List[String] = {
		handleCast(getContentTable(doc), extractActorUrls)
	}

	private def extractActorUrls(spanWithActorName: Element): List[String] = {
		List(spanWithActorName.parent().attr("href").split("\\?")(0))
	}
	private def extractRdfTriples(spanWithActorName: Element): List[RdfTriple] = {
		val imdbId = spanWithActorName.parent().attr("href").split("\\?")(0).replaceAll("\\D", "").toInt
		val actorName = spanWithActorName.html()
		val actor = RdfResource(s"lod:Actor$imdbId")
		List(actor isAn RdfMovieResource.actor,
			actor name actorName)
	}


	private def getContentTable(doc: Document): Element = {
		doc.select("#fullcredits_content").get(0)
	}

}
