package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.{RdfPersonResource, RdfTriple, RdfResource}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfMovieResource._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.DateTime

class ImdbActorTriplifier(val imdbId: String) {

	def triplify(content: String): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val doc = Jsoup.parse(content)
		val overviewDiv = doc.getElementById("name-overview-widget-layout")

		if (overviewDiv == null)
			return List()

		triplifyActor(overviewDiv)
	}

	def triplifyActor(overviewDiv: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val actor = RdfResource(s"lod:MoviePerson$imdbId")
		triples = List(actor isAn RdfPersonResource.actor, actor isA RdfPersonResource.person) ::: triples

		val image = overviewDiv.select(".image a img").attr("src")
		if (!image.isEmpty)
			triples = (actor hasImage image) :: triples

		val actorName = overviewDiv.select(".header span").text()
		if (!actorName.isEmpty)
			triples = List(actor name actorName, actor hasLabel actorName) ::: triples

		triples = handleDescription(overviewDiv, actor) ::: triples
		triples = handleBirthInfo(overviewDiv, actor) ::: triples

		triples
	}

	def handleDescription(overviewDiv: Element, actor: RdfPersonResource): List[RdfTriple] = {
		val descriptionDiv = overviewDiv.select(".name-trivia-bio-text div")
		if (descriptionDiv.size() == 1) {
			val description = descriptionDiv.first().ownText()
			if (!description.isEmpty)
				return List(actor abstractContent description)
		}

		List()
	}

	def handleBirthInfo(overviewDiv: Element, actor: RdfPersonResource): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()
		val birthInfo = overviewDiv.select("#name-born-info a")

		var year, monthday : String = null
		birthInfo.foreach { a: Element =>
			val href = a.attr("href").split("_").last
			href match {
				case "nm" => triples = (actor hasBirthName a.text()) :: triples
				case "place" => triples = (actor hasBirthPlace  a.text()) :: triples
				case "year" => year = a.text()
				case "monthday" => monthday = a.text()
				case _ =>
			}
		}

		if (year != null && monthday != null) {
			val formatter = DateTimeFormat.forPattern("MMM dd YYYY")
			val date = formatter.parseDateTime(s"$monthday $year")

			triples = (actor born date) :: triples
		}

		triples
	}
}
