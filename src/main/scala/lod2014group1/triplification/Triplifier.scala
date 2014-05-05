package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File

class Triplifier {

	def triplify(f: File): List[RdfTriple] = {
		if (f.getName == "fullcredits.html")
			new ImdbCastTriplifier().triplify(f)
		else if (f.getName == "locations.html")
			new ImdbLocationTriplifier().triplify(f)
		else if (f.getName == "keywords.html")
			new ImdbKeywordTriplifier().triplify(f)
		else
			List()
	}

}
