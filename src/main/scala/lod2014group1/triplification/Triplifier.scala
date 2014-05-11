package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File

class Triplifier {

	def triplify(f: File): List[RdfTriple] = {
		val imdbId = f.getPath().split("/"){2}.replaceAll("\\D", "")

		if (f.getName == "fullcredits.html")
			new ImdbCastTriplifier().triplify(f)
		else if (f.getName == "locations.html")
			new ImdbLocationTriplifier().triplify(f)
		else if (f.getName == "keywords.html")
			new ImdbKeywordTriplifier(imdbId).triplify(f)
		else
			List()
	}
}

object Triplifier {
	def go() {
		val triplifier = new Triplifier
//		val triples = triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
		val triples = triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
		triples.take(10).foreach(println)

		//triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
		//triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html"))

	}
}
