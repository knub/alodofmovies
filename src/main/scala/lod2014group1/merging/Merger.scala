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

	def mergeReleaseInfoTriple(imdbMovieResource: String, releaseInfoTriple: List[RdfTriple]): List[RdfTriple] = {
		addConnectionTripleToMovie(imdbMovieResource, releaseInfoTriple, "lod:ReleaseInfo", "dbpprop:released")
	}

	def mergeAkaTriple(imdbMovieResource: String, akaTriple: List[RdfTriple]): List[RdfTriple] = {
		addConnectionTripleToMovie(imdbMovieResource, akaTriple, "lod:aka", "dbpprop:alternativeNames")
	}

  def replaceSubjectAndObject(subjectResource: String, objectResource: String, triples: List[RdfTriple]): List[RdfTriple] = {
    var additionalTriples: List[RdfTriple] = List()
    triples.foreach { triple =>
      additionalTriples ::= RdfTriple(RdfResource(subjectResource), triple.p, RdfResource(objectResource))
    }
    additionalTriples
  }

  private def getAdditionalTriples(resource: String, triples: List[RdfTriple], excludeTriples: List[String]): List[RdfTriple] = {
    var additionalTriples: List[RdfTriple] = List()
    triples.foreach { triple =>
      if (!excludeTriples.contains(triple.p.toString())) {
        additionalTriples ::= RdfTriple(RdfResource(resource), triple.p, triple.o)
      }
    }
    additionalTriples
  }

	private def addConnectionTripleToMovie(movieResource: String, triples: List[RdfTriple], resourceType: String, connectionProperty: String): List[RdfTriple] = {
		var additionalTriples: List[RdfTriple] = List()

		triples.foreach { triple =>
			if (triple.p.toString().equals("rdf:type") && triple.o.toString.equals(resourceType)) {
				additionalTriples ::= new RdfTriple(RdfResource(movieResource), RdfResource(connectionProperty), triple.s)
			}
			additionalTriples ::= triple
		}
		additionalTriples
	}

	private def excludeTriple(triple: List[RdfTriple], excludeTriple: List[String]): List[RdfTriple] = {
		triple.filter { t =>
			!excludeTriple.contains(t.p.toString())
		}
	}

	private def otherPersonList: List[String] = {
		List(
			"dbpprop:cinematography",
			"dbpprop:music",
			"dbpprop:casting",
			"dbpprop:stunts/acting",
			"dbpprop:productionDesigner",
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
			"dbpprop:costume",
			"dbpprop:setDecorator",
			"dbpprop:otherCrew"
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

	private def excludeOtherPersonTripleList: List[String] = {
		List(
			"lod:hasAward"
		)
	}
}
