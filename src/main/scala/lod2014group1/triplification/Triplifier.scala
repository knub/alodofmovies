package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File

abstract class Triplifier{

	def triplify(content: String): List[RdfTriple]
}
