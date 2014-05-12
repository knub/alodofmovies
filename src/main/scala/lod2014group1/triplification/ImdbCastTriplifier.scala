package lod2014group1.triplification

import lod2014group1.rdf._
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.{Document, Element}
import lod2014group1.rdf.RdfActorResource._
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import org.jsoup.select.Elements
import org.slf4s.Logging

class ImdbCastTriplifier extends Logging {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)
		try {
			val table = getContentTable(doc)
			triplifyCast(table)
		} catch {
			case e: IndexOutOfBoundsException => {
				log.error("No content table found in " + f.getAbsolutePath)
				List()
			}
			case e: Throwable => {
				log.error("Error in  " + f.getAbsolutePath)
				log.error(e.getStackTraceString)
				List()
			}
		}
	}

	def triplifyCast(contentTable: Element) = {
		val tables = contentTable.select("#fullcredits_content")
		val castTable = tables.select("table.cast_list")
		if (castTable.size() > 1)
			throw new RuntimeException("More than one cast list!")

		if (castTable.isEmpty)
			List()
		else {
			castTable.select("tr").toList.flatMap { row =>
				if (row.select("a").isEmpty)
					List()
				else {
					val imdbIdString = row.select("a").first.attr("href").split("\\?")(0)
					val imdbId = imdbIdString.replaceAll("\\D", "").toInt
					val actor = RdfResource(s"lod:Actor$imdbId")

					(actor hasImdbUrl imdbIdString) ::
					extractActorTriples(actor, row.select("a span").get(0)) :::
					extractCharacterTriples(actor, row.select("td.character"))
				}
			}
		}
	}

	def getActorUrls(f: File): List[String] = {
		triplify(f).flatMap { triple =>
			if (triple.p.uri == "lod:imdbUrl")
				List(triple.o.asInstanceOf[RdfUrl].url)
			else
				List()
		}
	}

	private def extractActorTriples(actor: RdfResource, spanWithActorName: Element): List[RdfTriple] = {
		val actorName = spanWithActorName.html()
		List(actor isAn actor,
			actor name actorName)
	}

	private def extractCharacterTriples(actor: RdfResource, characterTableCells: Elements): List[RdfTriple] = {
		characterTableCells.toList.flatMap { characterTd =>
			val link = characterTd.select("a")
			if (link.isEmpty)
				List()
			else {
				val characterId = link.get(0).attr("href").split("/")(2).substring(2)
				val character = RdfResource(s"lod:Character$characterId")
				List(actor playsRole character)
			}
		}
	}


	private def getContentTable(doc: Document): Element = {
		doc.select("#fullcredits_content").get(0)
	}

}
