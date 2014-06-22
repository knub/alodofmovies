package lod2014group1.merging

import lod2014group1.database.Queries
import lod2014group1.rdf.RdfTriple

object Merger {

	def mergeMovieTriple(allTriples: List[RdfTriple]): List[RdfTriple] = {
		var additionalTriples: List[RdfTriple] = List()

		allTriples.foreach{ triple =>
			if (triple.addAlwaysFlag) {
				additionalTriples ::= triple
			} else {
				// if a triple with this predicate already exists, do not add the triple
				val existing = Queries.getObjects(triple.s.toString(), triple.p.toString()).size > 0
				if (!existing) {
					additionalTriples ::= triple
				}
			}
		}

		additionalTriples
	}
}
