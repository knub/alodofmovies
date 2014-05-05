package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConversions._
import org.jsoup.nodes.Element

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

}
