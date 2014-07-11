package lod2014group1.merging

import lod2014group1.rdf.{RdfResource, RdfTriple}
import scalax.collection.edge.LDiEdge

class TripleGraph(triples: List[RdfTriple]) {
	val edges = triples.map { triple =>
		LDiEdge(triple.s.toString().trim, prepareString(triple.o.toString))(triple.p.toString())
	}

	def prepareString(s: String): String = {
		if (s.head == '"' && s.last == '"')
			s.stripPrefix("\"").stripSuffix("\"")
		else s
	}

	def getObjectOfType(rdfType: String): String = {
		edges.find { edge =>
        edge.label.toString == "rdf:type" &&
          edge.target.toString == rdfType
    } match {
      case Some(e) => e.source
      case None => null
    }
	}
	
	def getObjectListOfType(rdfType: String): List[String] = {
		edges.filter { edge =>
			edge.label.toString == "rdf:type" &&
				edge.target.toString == rdfType
		}.map {o => o.source}
	}

	def getObjectsFor(query1: String, query2: String): List[String] = {
		getObjectsForPredicate(query1).flatMap { o =>
			getObjectsForSubjectAndPredicate(o, query2)
		}
	}

	def getObjectsForPredicate(predicate: String) : List[String] = {
		val s = edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target
		}.toList
	}

	def getObjectsForSubjectAndPredicate(subject: String, predicate: String) : List[String] = {
		val s = edges.filter { edge =>
			edge.source == subject &&
				edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target
		}.toList
	}
	

	def getTriplesForSubjectAndPredicate(subject: String, predicate: String) : List[RdfTriple] = {
		val s = edges.filter { edge =>
			edge.source == subject &&
				edge.label.toString.contains(predicate)
		}

		s.flatMap { edge =>
			getTriplesForSubject(edge.target)
		}.toList
	}
	
	def getTriplesForSubjectAndObject(subject: String, objectString: String) : List[RdfTriple] = {
		val s = edges.filter { edge =>
			edge.source == subject &&
				edge.target == objectString
		}

		s.map { edge =>
			RdfTriple(RdfResource(edge.source), RdfResource(edge.label.toString), RdfResource(edge.target))
		}.toList
	}

	def getTriplesForSubject(subject: String) : List[RdfTriple] = {
		val s = edges.filter { edge =>
			edge.source == subject
		}

		s.map { edge =>
			RdfTriple(RdfResource(edge.source), RdfResource(edge.label.toString), RdfResource(edge.target))
		}.toList
	}
	
	def getImdbId(): String = {
		val sameAsTriples = getObjectsForPredicate("owl:sameAs").filter(p => p.contains("imdb.com/title/"))
		if (sameAsTriples.isEmpty)
			return null

    var id = sameAsTriples.head.split("imdb.com/title/").last
    if (id.length < 9) {
      println(sameAsTriples.head)
      return null
    }

    id = id.substring(0, 9)

    if (id.matches("tt[0-9]{7}"))
      id
    else {
      println(sameAsTriples.head)
      return null
    }
	}
}
