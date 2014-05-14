package lod2014group1.triplification

import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.rdf.RdfAwardResource._
import lod2014group1.rdf.RdfActorResource._
import lod2014group1.rdf._
import lod2014group1.rdf
import java.lang.ArrayIndexOutOfBoundsException

class ImdbAwardTriplifier(val imdbId: String) {

	var name = ""
	var country = ""
	var year = ""
	var outcome = ""
	var category = ""
	var description = ""
	var nominee = ""
	var nomineeUri = ""
	var details = ""
	var role = ""
	var awardCount = 1

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)
		val awardTables = doc.getElementsByClass("awards")

		var triple: List[RdfTriple] = List()
		awardTables.foreach(award => {
			triple = triplifyAward(award) ::: triple
		})

		triple
	}

	def triplifyAward(table: Element): List[RdfTriple] = {
		var triples: List[RdfTriple] = List()

		val nameCountryYear = table.previousElementSibling().text();
		name = getName(nameCountryYear)
		country = getCountry(nameCountryYear)
		year = getYear(nameCountryYear)

		table.select("td").foreach(td => {
			if (td.hasClass("title_award_outcome")) {
				outcome = td.select("b").text()
				category = td.select(".award_category").text()
			}

			if (td.hasClass("award_description")) {
				description = td.select(".award_description").first().ownText()

				if (td.children().size() == 1) {
					triples = handleAward() ::: triples
					awardCount += 1
				}

				td.select("a").foreach(a => {
					triples = handelElement(a) ::: triples
				})
			}
		})

		triples
	}

	def handelElement(a: Element): List[RdfTriple] = {
		nominee = a.text()
		nomineeUri = a.attr("href")

		// If the nominee is "More", the tag does not include a nominee
		if (nominee == "More")
			return List()

		val roleSibling = a.nextElementSibling()
		var detailSibling: Element = a

		if (roleSibling != null) {
			if (roleSibling.hasClass("production_role")) {
				role = roleSibling.text();
				detailSibling = roleSibling
			}
		}

		// the details are in the second sibling element
		detailSibling = detailSibling.nextElementSibling()
		if (detailSibling != null) {
			detailSibling = detailSibling.nextElementSibling()
			if (detailSibling != null) {
				// check if this elements is a award_detail_notes element
				if (detailSibling.hasClass("award_detail_notes")) {
					if (detailSibling.children().size() == 2) {
						details = detailSibling.select(".full-note").text()
					} else {
						details = detailSibling.text()
					}
				}
			}
		}

		// create triples
		val triples = handleAward()
		awardCount += 1

		triples
	}


	def handleAward(): List[RdfTriple] = {
		var movie = RdfResource(s"lod:Movie$imdbId")
		val award = RdfResource(s"lod:Movie$imdbId/Award$awardCount")

		var triples = List(
			 movieResourceFromRdfResource(movie) hasAward award,
			 award isAn RdfAwardResource.award,
			 award name name,
			 award outcome outcome,
			 award category category
		)

		if (nominee.isEmpty) {
			triples = (award nominee movie) :: triples
		} else {
			val actorNr = nomineeUri.split("/")(2).split("\\?")(0).substring(2)
			val actor = RdfResource(s"lod:Actor$actorNr")

			triples = List(award nominee actor, actorResourceFromRdfResource(actor) hasAward award) ::: triples
		}

		if (! description.isEmpty)
			triples = (award description description) :: triples

		if (! role.isEmpty)
			triples = (award role role) :: triples

		if (! country.isEmpty)
			triples = (award country country) :: triples

		if (! year.isEmpty)
			triples = (award year year) :: triples

		if (! details.isEmpty)
			triples = (award details details) :: triples

		details = ""
		role = ""
		nominee = ""
		nomineeUri = ""

		triples
	}


	def getName(str: String): String = {
		if (str.contains(","))
			str.split(","){0}
		else
			str.dropRight(5)
	}
	def getCountry(str: String): String = {
		if (str.contains(","))
			str.split(","){1}.split(" "){1}
		else
			""
	}
	def getYear(str: String): String = {
		val year = str takeRight(4)
		if (isNumber(year))
			year
		else
			""
	}

	def isNumber(x: String) = x forall Character.isDigit
}