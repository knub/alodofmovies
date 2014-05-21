package lod2014group1.triplification

import lod2014group1.rdf._
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.{Document, Element}
import lod2014group1.rdf.RdfActorResource._
import lod2014group1.rdf.RdfCharacterResource._
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import org.jsoup.select.Elements
import org.slf4s.Logging

class ImdbCastTriplifier(val imdbId: String) extends Logging {

	val movie = RdfResource(s"lod:Movie$imdbId")

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)
		try {
			val table = doc.select("div#fullcredits_content").get(0)
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

	def triplifyCast(contentTable: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val groups = contentTable.select("h4.dataHeaderWithBorder")
		groups.foreach(group => {
			val groupName = group.ownText()
			if (groupName.startsWith("Directed by")) { triples = handleDirector(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Writing Credits")) { triples = handleWriter(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Produced by")) { triples = handleProducer(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Music by")) { triples = handleMusic(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Film Editing by")) { triples = handleEditing(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Cast") && !groupName.startsWith("Casting")) { triples = handleActor(group.nextElementSibling()) ::: triples }
		})

		triples
	}

	def handleDirector(table: Element): List[RdfTriple] = {
		List()
	}

	def handleWriter(table: Element): List[RdfTriple] = {
		List()
	}

	def handleProducer(table: Element): List[RdfTriple] = {
		List()
	}

	def handleMusic(table: Element): List[RdfTriple] = {
		List()
	}

	def handleEditing(table: Element): List[RdfTriple] = {
		List()
	}

	def handleActor(castTable: Element): List[RdfTriple] = {
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
		List(actor isAn RdfActorResource.actor,
			actor name actorName,
			movie playedBy actor)
	}

	private def extractCharacterTriples(actor: RdfResource, characterTableCells: Elements): List[RdfTriple] = {
		characterTableCells.toList.flatMap { characterTd =>
			val link = characterTd.select("a")
			if (link.isEmpty)
				List()
			else {
				val characterId = link.get(0).attr("href").split("/")(2).substring(2)
				val character = RdfResource(s"lod:Character$characterId")
				val characterName = link.text()

				List(character name characterName,
					actor playsCharacter character,
					character inMovie movie,
					character playedBy actor,
					character isA RdfCharacterResource.character)
			}
		}
	}
}
