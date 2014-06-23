package lod2014group1.merging

import lod2014group1.database.Queries
import lod2014group1.rdf.{RdfResource, RdfTriple}

object Merger {

	def mergeMovieTriple(imdbMovieResource: String, movieTriples: List[RdfTriple]): List[RdfTriple] = {
		getAdditionalTriples(imdbMovieResource, movieTriples)
	}

	def mergeActorTriple(imdbActorResource: String, actorTriples: List[RdfTriple]): List[RdfTriple] = {
		getAdditionalTriples(imdbActorResource, actorTriples)
	}

	private def getAdditionalTriples(resource: String, triples: List[RdfTriple]): List[RdfTriple] = {
		var additionalTriples: List[RdfTriple] = List()

		triples.foreach{ triple =>
			if (triple.addAlwaysFlag) {
				additionalTriples ::= triple
			} else {
				// if a triple with a specific predicate already exists, do not add the triple
				if (!Queries.existsTriple(resource, triple.p.toString())) {
					additionalTriples ::= RdfTriple(RdfResource(resource), triple.p, triple.o)
				}
			}
		}
		additionalTriples
	}

	def mergeReleaseInfoTriple(imdbMovieResource: String, releaseInfoTriple: List[RdfTriple]): List[RdfTriple] = {
		if (Queries.existsReleaseInfo(imdbMovieResource)) {
			return List()
		}
		addConnectionTriples(imdbMovieResource, releaseInfoTriple, "lod:ReleaseInfo", "dbpprop:released")
	}

	def mergeAkaTriple(imdbMovieResource: String, akaTriple: List[RdfTriple]): List[RdfTriple] = {
		if (Queries.existsAka(imdbMovieResource)) {
			return List()
		}
		addConnectionTriples(imdbMovieResource, akaTriple, "lod:aka", "dbpprop:alternativeNames")
	}

	def mergeAwardTriple(imdbMovieResource: String, awardTriple: List[RdfTriple]): List[RdfTriple] = {
		if (Queries.existsAward(imdbMovieResource)) {
			return List()
		}
		addConnectionTriples(imdbMovieResource, awardTriple, "dbpedia-owl:Award", "lod:hasAward")
	}

	private def addConnectionTriples(movieResource: String, triples: List[RdfTriple], resourceType: String, connectionProperty: String): List[RdfTriple] = {
		var additionalTriples: List[RdfTriple] = List()
		triples.foreach{ triple =>
			if (triple.p.toString().equals("rdf:type") && triple.o.toString.equals(resourceType)) {
				additionalTriples ::= new RdfTriple(RdfResource(movieResource), RdfResource(connectionProperty), triple.s)
			}
			additionalTriples ::= triple
		}
		additionalTriples
	}
}
