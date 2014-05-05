package lod2014group1.triplification

import lod2014group1.rdf.RDFTriple
import java.io.File

class Triplifier {

	def triplify(f: File): List[RDFTriple] = {
		if (f.getName == "fullcredits.html")
			new IMDBCastTriplifier().triplify(f)
		else if (f.getName == "locations.html")
			new IMDBLocationTriplifier().triplify(f)
		else if (f.getName == "keywords.html")
			new IMDBKeywordTriplifier().triplify(f)
		else
			List()
	}

}
