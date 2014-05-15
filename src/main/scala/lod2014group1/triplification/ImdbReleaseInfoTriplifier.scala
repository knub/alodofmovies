package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.{RdfReleaseInfoResource, RdfTriple, RdfResource}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConversions._
import lod2014group1.rdf.RdfReleaseInfoResource._
import lod2014group1.rdf.RdfAkaResource._
import lod2014group1.rdf.RdfMovieResource._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import lod2014group1.rdf

class ImdbReleaseInfoTriplifier(val imdbId: String) {

	var releaseInfoCount = 1
	var akaCount = 1

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		val releaseInfoTable= doc.select("table#release_dates")
		val akaTable = doc.select("table#akas")

		var triple: List[RdfTriple] = List()

		releaseInfoTable.select("tr").foreach(releaseInfo => {
			triple = triplifyReleaseInfo(releaseInfo) ::: triple
		})

		akaTable.select("tr").foreach(aka => {
			triple = triplifyAka(aka) ::: triple
		})

		triple
	}

	def triplifyReleaseInfo(releaseInfo: Element): List[RdfTriple] = {
		val td = releaseInfo.select("td")

		val country = td(0).text()
		val description = td(2).text()

		val triples = handleReleaseInfo(country, description) ::: handleDate(td(1).text())

		releaseInfoCount += 1

		triples
	}

	def triplifyAka(aka: Element): List[RdfTriple] = {
		val td = aka.select("td")

		val country = td(0).text().split("\\(")(0).trim
		val description = getAkaDescription(td(0).text())
		val name = td(1).text()

		val triples = handleAka(name, country, description)

		akaCount += 1

		triples
	}

	def getAkaDescription(str: String): String = {
		val parts = str.split("\\(")

		if (parts.length > 1) {
			var description = parts(1).drop(0).split("\\)")(0)
			if (parts.length > 2) {
				parts.drop(0).drop(0).foreach(desc => {
					description += "; " + desc.split("\\)")(0)
				})
			}
			description
		} else {
			""
		}
	}

	def handleDate(dateStr: String): List[RdfTriple] = {
		val releaseInfo = RdfResource(s"lod:Movie$imdbId/ReleaseInfo$releaseInfoCount")

		val dateComponents = dateStr.split(" ").length
		if (dateComponents == 3) {
			val formatter = DateTimeFormat.forPattern("dd MMM yyyy");
			val date = formatter.parseDateTime(dateStr);

			List(releaseInfo atDate date)

		} else {
			List(releaseInfo atDate dateStr)
		}
	}

	def handleReleaseInfo(country: String, description: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")
		val releaseInfo = RdfResource(s"lod:Movie$imdbId/ReleaseInfo$releaseInfoCount")

		var triples = List(
			movie hasReleaseInfo releaseInfo,
			releaseInfo isA RdfReleaseInfoResource.releaseInfo,
			releaseInfo inCountry country
		)

		if (! description.isEmpty) {
			triples = (releaseInfo hasDescription description) :: triples
		}

		triples
	}

	def handleAka(name: String, country: String, description: String): List[RdfTriple] = {
		val movie = RdfResource(s"lod:Movie$imdbId")
		val aka = RdfResource(s"lod:Movie$imdbId/Aka$akaCount")

		var triples = List(
			movie alsoKnownAs aka,
			aka isAn alternativeMovieName,
			aka hasAkaName name
		)

		if (! description.isEmpty) {
			triples = (aka hasDescription description) :: triples
		}

		if (! country.isEmpty) {
			triples = (aka inCountry country) :: triples
		}

		triples
	}

	def isAllDigits(x: String) = x forall Character.isDigit

}