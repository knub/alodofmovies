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
import org.apache.commons.io.FileUtils

class ImdbCastTriplifier(val imdbId: String) extends Triplifier with Logging {

	val movie = RdfResource(s"lod:Movie$imdbId")

	def triplify(content: String): List[RdfTriple] = {
		val doc = Jsoup.parse(content)
		try {
			val table = doc.select("div#fullcredits_content").get(0)
			triplifyCast(table)
		} catch {
			case e: IndexOutOfBoundsException =>
				log.error("No content table found in " + doc.select("title").html)
				List()
			case e: Throwable =>
				log.error("Error in  " + doc.select("title").html)
				log.error(e.getStackTraceString)
				List()
		}
	}

	def triplifyCast(contentTable: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val groups = contentTable.select("h4.dataHeaderWithBorder")
		groups.foreach(group => {
			val groupName = group.ownText()
			val groupTable = group.nextElementSibling()

			groupName match {
				case name if name.startsWith("Directed by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:director", RdfPersonResource.director) ::: triples
				case name if name.startsWith("Writing Credits") =>
					triples = handleWriter(groupTable) ::: triples
				case name if name.startsWith("Produced by") =>
					triples = handleProducer(groupTable) ::: triples
				case name if name.startsWith("Music by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:music", null) ::: triples
				case name if name.startsWith("Cinematography by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:cinematography", null) ::: triples
				case name if name.startsWith("Film Editing by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:editing", null) ::: triples
				case name if name.startsWith("Casting by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:casting", null) ::: triples
				case name if name.startsWith("Production Design by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:productionDesigner", null) ::: triples
				case name if name.startsWith("Art Direction by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:artDirector", null) ::: triples
				case name if name.startsWith("Set Decoration by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:setDecorator", RdfPersonResource.setDesigner) ::: triples
				case name if name.startsWith("Costume Design by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:costumeDesign", RdfPersonResource.costumeDesigner) ::: triples
				case name if name.startsWith("Makeup Department") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:makeupArtist", RdfPersonResource.makeUpArtist) ::: triples
				case name if name.startsWith("Production Management") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:productionManager", null) ::: triples
				case name if name.startsWith("Special Effects by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:specialEffects", RdfPersonResource.specialEffects) ::: triples
				case name if name.startsWith("Visual Effects by") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:visualEffects", null) ::: triples
				case name if name.startsWith("Stunts") =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:stunts/acting", null) ::: triples
				case name if name.startsWith("Cast") && !name.startsWith("Casting") =>
					triples = handleActor(groupTable) ::: triples
				case _ =>
					triples = extractTriplesForGroup(groupTable, "dbpprop:otherCrew", null) ::: triples
			}
		})


		triples
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
						case str if str.contains("story") => triples = (movie storyBy name) :: triples
						case str if str.contains("novel") => triples = (movie novelBy writer) :: triples
						case str if str.contains("screenplay") => triples = (movie screenplayBy name) :: triples
						case _ => triples = (movie writtenBy name) :: triples
					}
				} else {
					val writer = getPersonResource(url)
					val id = url.split("\\?")(0).split("/").last

					triples = List(
						writer hasName name,
						writer hasLabel name,
						writer isA RdfPersonResource.writer,
						writer isA RdfPersonResource.person,
						writer hasImdbUrl id
					) ::: triples

					credit match {
						case str if str.contains("story") => triples = List(movie storyBy writer, writer hasJob "story") ::: triples
						case str if str.contains("novel") => triples = List(movie novelBy writer, writer hasJob "novel") ::: triples
						case str if str.contains("screenplay") => triples = List(movie screenplayBy writer, writer hasJob "screenplay") ::: triples
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
						case "co-producer" => triples = List(movie coProducedBy name, producer isA RdfPersonResource.coProducer) ::: triples
						case _ => triples = List(movie producedBy name, producer isA RdfPersonResource.producer) ::: triples
					}
				} else {
					val producer = getPersonResource(url)
					val id = url.split("\\?")(0).split("/").last

					triples = List(
						producer hasName name,
						producer hasLabel name,
						producer isA RdfPersonResource.person,
						producer hasImdbUrl id
					) ::: triples

					val job = getJob(credit)
					if (job != null)
						triples = (producer hasJob job) :: triples

					val alternatvieName = getAlias(credit)
					if (alternatvieName != null)
						triples = (person hasAlternativeName alternatvieName) :: triples

					credit match {
						case "co-producer" => triples = (movie coProducedBy producer) :: triples
						case _ => triples = (movie producedBy producer) :: triples
					}
				}
			}
		})

		triples
	}

	def handleActor(castTable: Element): List[RdfTriple] = {
		castTable.select("tr").toList.flatMap { row =>
			if (row.select("a").isEmpty)
				List()
			else {
				val imdbIdString = row.select("a").first.attr("href").split("\\?")(0).split("/").last
				val actor = RdfResource(s"lod:MoviePerson$imdbIdString")

				(actor hasImdbUrl imdbIdString) ::
					extractActorImageTriples(actor, row.select(".primary_photo img").get(0)) :::
					extractActorTriples(actor, row.select("a span").get(0)) :::
					extractCharacterTriples(actor, row.select("td.character"))
			}
		}
	}

	private def extractActorImageTriples(actor: RdfResource, imgTag: Element): List[RdfTriple] = {
		List(actor hasImage imgTag.attr("src"))
	}

	private def extractActorTriples(actor: RdfResource, spanWithActorName: Element): List[RdfTriple] = {
		val actorName = spanWithActorName.html()
		List(actor isAn RdfPersonResource.actor,
			actor isA RdfPersonResource.person,
			actor name actorName,
			actor hasLabel actorName,
			movieResourceFromRdfResource(movie) starring actor)
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
				val characterId = link.get(0).attr("href").split("/")(2)
				val characterName = link.text()
				val characterMovie = RdfResource(s"lod:Movie$imdbId/Character$characterId")
				val character = RdfResource(s"lod:Character$characterId")

				triples = List(character name characterName,
					characterMovie name characterName,
					character hasLabel characterName,
					characterMovie hasLabel characterName,
					actor playsCharacter characterMovie,
					characterMovie inMovie movie,
					characterInfoResourceFromRdfResource(characterMovie) playedBy actor,
					characterMovie isA RdfCharacterResource.character,
					character isA RdfCharacterResource.character,
					characterMovie isSubclassOf character) ::: triples
			}
		})
		triples
	}


	def extractTriplesForGroup(table: Element, property: String, rdfType: RdfResource): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		table.select("tr").foreach(tr => {
			val name = extractName(tr)
			val url = extractUrl(tr)
			val credit = extractCredit(tr)

			if (!name.isEmpty) {
				if (url.isEmpty) {
					return List(RdfTriple(movie, RdfResource(property), RdfString(name)))
				} else {
					val person = getPersonResource(url)
					val id = url.split("\\?")(0).split("/").last

					triples = List (
						person hasName name,
						person hasLabel name,
						person isA RdfPersonResource.person,
						person hasImdbUrl id
					) ::: triples

					val job = getJob(credit)
					if (job != null)
						triples = (person hasJob job) :: triples

					if (rdfType != null) {
						triples = RdfTriple(person, RdfResource("rdf:type"), rdfType) :: triples
					}
					triples = RdfTriple(movie, RdfResource(property), person) :: triples

					val alternatvieName = getAlias(credit)
					if (alternatvieName != null)
						triples = (person hasAlternativeName alternatvieName) :: triples
				}
			}
		})

		triples
	}


	def getAlias(str: String): String = {
		if (str.contains("(as"))
			return str.split("\\(as ")(1).split("\\)")(0)
		null
	}

	def getJob(str: String): String = {
		if (str.isEmpty)
			return null

		if (str.contains("(as"))
			str.split("\\(as ")(0).dropRight(1)
		str
	}


	def getPersonResource(url: String): RdfResource = {
		val id = url.split("\\?")(0).split("/").last
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
		val content = FileUtils.readFileToString(f, "UTF-8")
		triplify(content).flatMap { triple =>
			// Only actors so far,
			if (triple.p.uri == "owl:sameAs")
				List(triple.o.asInstanceOf[RdfUrl].lit.replace("<", "").replace(">", ""))
			else
				List()
		}
	}

}
