package lod2014group1.triplification

import lod2014group1.rdf._
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.{Document, Element}
import lod2014group1.rdf.RdfPersonResource._
import lod2014group1.rdf.RdfCharacterResource._
import lod2014group1.rdf.RdfMovieResource._
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
			else if (groupName.startsWith("Cinematography by")) { triples = handleCinematography(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Film Editing by")) { triples = handleFilmEditing(group.nextElementSibling()) ::: triples }
			else if (groupName.startsWith("Cast") && !groupName.startsWith("Casting")) { triples = handleActor(group.nextElementSibling()) ::: triples }
		})

		triples
	}

	def handleDirector(table: Element): List[RdfTriple] = {
		table.select("tr").toList.flatMap(tr => {
			buildNameAndUrlTriple(tr, "dbpprop:director", RdfPersonResource.director)
		})
	}

	def handleWriter(table: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		table.select("tr").foreach(tr => {
			val name = extractName(tr)
			val url = extractUrl(tr)
			val credit = extractCredit(tr)

			if (!name.isEmpty) {
				if (url.isEmpty) {
					credit match {
						case "(story)" => triples = (movie stroyBy name) :: triples
						case "(novel)" => triples = (movie novelBy writer) :: triples
						case "(screenplay)" => triples = (movie screenplayBy name) :: triples
						case _ => triples = (movie writtenBy name) :: triples
					}
				} else {
					val writer = getPersonResource(url)

					triples = List(
						writer hasName name,
						writer isA RdfPersonResource.writer,
						writer isA RdfPersonResource.person
					) ::: triples

					credit match {
						case "(story)" => triples = (movie stroyBy writer) :: triples
						case "(novel)" => triples = (movie novelBy writer) :: triples
						case "(screenplay)" => triples = (movie screenplayBy writer) :: triples
						case _ => triples = (movie writtenBy writer) :: triples
					}
				}
			}
		})

		triples
	}

	def handleProducer(table: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		table.select("tr").foreach(tr => {
			val name = extractName(tr)
			val url = extractUrl(tr)
			val credit = extractCredit(tr)

			if (!name.isEmpty) {
				if (url.isEmpty) {
					credit match {
						case "co-producer" => triples = (movie coProducedBy name) :: triples
						case _ => triples = (movie producedBy name) :: triples
					}
				} else {
					val producer = getPersonResource(url)

					triples = List(
						producer hasName name,
						producer isA RdfPersonResource.producer,
						producer isA RdfPersonResource.person
					) ::: triples

					credit match {
						case "co-producer" => triples = (movie coProducedBy producer) :: triples
						case _ => triples = (movie producedBy producer) :: triples
					}
				}
			}
		})

		triples
	}

	def handleMusic(table: Element): List[RdfTriple] = {
		table.select("tr").toList.flatMap(tr => {
			buildNameAndUrlTriple(tr, "dbpprop:music", null)
		})
	}

	def handleCinematography(table: Element): List[RdfTriple] = {
		table.select("tr").toList.flatMap(tr => {
			buildNameAndUrlTriple(tr, "dbpprop:cinematography", null)
		})
	}

	def handleFilmEditing(table: Element): List[RdfTriple] = {
		table.select("tr").toList.flatMap(tr => {
			buildNameAndUrlTriple(tr, "dbpprop:editing", null)
		})
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
					extractActorImageTriples(actor, row.select(".primary_photo img").get(0)) :::
					extractActorTriples(actor, row.select("a span").get(0)) :::
					extractCharacterTriples(actor, row.select("td.character"))
			}
		}
	}

	private def extractActorImageTriples(actor: RdfResource, imgTag: Element): List[RdfTriple] = {
		List(actor hasImage imgTag.attr("scr"))
	}

	private def extractActorTriples(actor: RdfResource, spanWithActorName: Element): List[RdfTriple] = {
		val actorName = spanWithActorName.html()
		List(actor isAn RdfPersonResource.actor,
			actor isA RdfPersonResource.person,
			actor name actorName,
			movie playedBy actor)
	}

	private def extractCharacterTriples(actor: RdfResource, characterTableCells: Elements): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()
		characterTableCells.foreach (characterTd => {
			val tdtext = characterTd.select("div").first.ownText()
			if (tdtext.contains("(as ")) {
				val alternativeName = getAlias(tdtext)
				triples = (actor hasAlternativeName alternativeName) :: triples
			}

			val link = characterTd.select("a")
			if (!link.isEmpty) {
				val characterId = link.get(0).attr("href").split("/")(2).substring(2)
				val character = RdfResource(s"lod:Character$characterId")
				val characterName = link.text()

				triples = List(character name characterName,
					actor playsCharacter character,
					character inMovie movie,
					character playedBy actor,
					character isA RdfCharacterResource.character) ::: triples
			}
		})
		triples
	}


	def buildNameAndUrlTriple(tr: Element, property: String, rdfType: RdfResource): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val name = extractName(tr)
		val url = extractUrl(tr)
		val credit = extractCredit(tr)

		if (!name.isEmpty) {
			if (url.isEmpty) {
				return List(RdfTriple(movie, RdfResource(property), RdfString(name)))
			} else {
				val person = getPersonResource(url)

				triples = List (
					person hasName name,
					person isA RdfPersonResource.person
				) ::: triples

				if (rdfType != null) {
					triples = RdfTriple(person, RdfResource("rdf:type"), rdfType) :: triples
				}
				triples = RdfTriple(movie, RdfResource(property), person) :: triples
			}
		}

		triples
	}


	def getAlias(str: String): String = {
		str.split("\\(as ")(1).split("\\)")(0)
	}


	def getPersonResource(url: String): RdfResource = {
		val id = url.replaceAll("\\D", "")
		RdfResource(s"lod:MoviePerson$id")
	}

	def extractName(tr: Element): String = {
		val nameTd = tr.select("td.name")
		if (nameTd.size() > 0) {
			nameTd.text()
		} else {
			""
		}
	}

	def extractUrl(tr: Element): String = {
		val nameUrl = tr.select("td.name a[href]")
		if (nameUrl.size() > 0) {
			nameUrl.attr("href")
		} else {
			""
		}
	}

	def extractCredit(tr: Element): String = {
		val creditTd = tr.select("td.credit")
		if (creditTd.size() > 0) {
			creditTd.text()
		} else {
			""
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

}
