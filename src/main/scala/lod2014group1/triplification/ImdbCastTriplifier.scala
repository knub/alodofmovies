package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.{Document, Element}

class ImdbCastTriplifier {

	def triplify(f: File): List[RdfTriple] = {
		val doc = Jsoup.parse(f, null)

		val tables = doc.select("#fullcredits_content table")
		System.out.println(tables.size());

		triplifyCast(tables.get(2))


		List()
	}

	def triplifyCast(castTable: Element): List[RdfTriple] = {
		if (castTable.attr("class") != "cast_list")
			throw new RuntimeException("This is not a cast list.")

		castTable.select("td a span").foreach { spanWithActorName =>
			System.out.println(spanWithActorName.html)
			System.out.println(spanWithActorName.parent().attr("href").split("\\?")(0));
		}
		List()
	}

	def getActorUrls(f: File): List[String] = {
		getActorUrls(Jsoup.parse(f, null))
	}

	private def getActorUrls(doc: Document): List[String] = {
		val tables = doc.select("#fullcredits_content")
		val castList = tables.select("table.cast_list")
		if (castList.size() > 1)
			throw new RuntimeException("More than one cast list!")
		if (castList.isEmpty)
			List()
		else
			getActorUrls(castList.get(0))
	}

	private def getActorUrls(castTable: Element): List[String] = {
		if (castTable.attr("class") != "cast_list")
			throw new RuntimeException("This is not a cast list, the class is " + castTable.attr("class"))

		castTable.select("td a span").toList.map { spanWithActorName =>
			spanWithActorName.parent().attr("href").split("\\?")(0)
		}
	}

}
