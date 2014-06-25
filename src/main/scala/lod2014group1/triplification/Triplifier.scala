package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple

abstract class Triplifier{

	def triplify(content: String): List[RdfTriple]

}
