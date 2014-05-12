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
					handleAward()
				}

				td.select("a").foreach(a => {
					nominee = a.text()
					nomineeUri = a.attr("href")

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
					handleAward()
				})
			}
		})

		List()
	}

	def handleAward(): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")

		System.out.println("Name: " + name)
		System.out.println("Country: " + country)
		System.out.println("Year: " + year)
		System.out.println("Outcome: " + outcome)
		System.out.println("Category: " + category)
		System.out.println("Description: " + description)
		System.out.println("Nominee: " + nominee)
		System.out.println("Nominee URI: " + nomineeUri)
		System.out.println("Role: " + role)
		System.out.println("Details: " + details)
		System.out.println("-------------------------")

		details = ""
		role = ""
		nominee = ""
		nomineeUri = ""

		List()
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