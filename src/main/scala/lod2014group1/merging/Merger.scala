package lod2014group1.merging

import lod2014group1.database.Queries
import lod2014group1.rdf.{RdfResource, RdfTriple}

object Merger {

	def mergeMovieTriple(imdbMovieResource: String, movieTriples: List[RdfTriple]): List[RdfTriple] = {
		getAdditionalTriples(imdbMovieResource, movieTriples, excludeMovieTripleList)
	}

	def mergeActorTriple(imdbActorResource: String, actorTriples: List[RdfTriple]): List[RdfTriple] = {
		getAdditionalTriples(imdbActorResource, actorTriples, excludeActorTripleList)
	}

	private def getAdditionalTriples(resource: String, triples: List[RdfTriple], excludeTriples: List[String]): List[RdfTriple] = {
		var additionalTriples: List[RdfTriple] = List()

		triples.foreach { triple =>
			if (excludeTriples.contains(triple.p.toString())) {
				// do nothing
			} else if (triple.addAlwaysFlag) {
				additionalTriples ::= RdfTriple(RdfResource(resource), triple.p, triple.o)
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

//	def mergeAwardTriple(imdbMovieResource: String, awardTriple: List[RdfTriple]): List[RdfTriple] = {
//		if (Queries.existsAward(imdbMovieResource)) {
//			return List()
//		}
//		addConnectionTriples(imdbMovieResource, awardTriple, "dbpedia-owl:Award", "lod:hasAward")
//	}

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

//	def mergeOtherPersons(imdbMovieResource: String, graph: TripleGraph): List[RdfTriple] = {
//		otherPersonList.flatMap{ personPredicate =>
//			val triple = graph.getTriplesForSubjectAndPredicate(imdbMovieResource, personPredicate)
//			mergeOtherPersonTriple(imdbMovieResource, triple, personPredicate)
//		}
//	}

	private def mergeOtherPersonTriple(imdbMovieResource: String, personTriple: List[RdfTriple], predicate: String): List[RdfTriple] = {
		if (Queries.existsPerson(imdbMovieResource, predicate)) {
			return List()
		}
		addConnectionTriples(imdbMovieResource, personTriple, "dbpedia-owl:Person", predicate)
	}

	private def otherPersonList: List[String] = {
		List(
			"dbpprop:cinematography",
			"dbpprop:music",
			"dbpprop:casting",
			"dbpprop:stunts/acting",
			"dbpprop:productionDesign",
			"dbpprop:productionManager",
			"dbpprop:artDirector",
			"dbpprop:otherCrew",
			"dbpprop:director",
			"dbpprop:writer",
			"dbpprop:screenplay",
			"dbpprop:story",
			"dbpprop:author",
			"dbpprop:producer",
			"dbpprop:coProducer",
			"dbpprop:setDecoator",
			"dbpprop:makeUpArtist",
			"dbpprop:specialEffects",
			"dbpprop:visualEffects",
			"dbpprop:editing",
			"dbpprop:costume"
		)
	}

	private def excludeMovieTripleList: List[String] = {
		otherPersonList ::: List(
			"dbpprop:starring",
			"dbpprop:released",
			"dbpprop:alternativeNames",
			"lod:hasAward"
		)
	}

	private def excludeActorTripleList: List[String] = {
		List(
			"dbpprop:character",
			"lod:hasAward"
		)
	}
}
