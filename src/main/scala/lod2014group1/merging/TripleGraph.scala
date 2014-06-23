package lod2014group1.merging

import lod2014group1.rdf.{RdfResource, RdfTriple}
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph

class TripleGraph(triples: List[RdfTriple]) {
	val edges = triples.map { triple =>
		LDiEdge(triple.s.toString().trim, prepareString(triple.o.toString))(triple.p.toString())
	}

	def prepareString(s: String): String = {
		if (s.head == '"' && s.last == '"')
			s.stripPrefix("\"").stripSuffix("\"")
		else s
	}

	val g = Graph(edges: _*)

	def getObjectOfType(rdfType: String): String = {
		g.edges.find { edge =>
			edge.label.toString == "rdf:type" &&
				edge.target.toString == rdfType
		}.get.source.toString
	}

	def getObjectsFor(query1: String, query2: String): List[String] = {
		getObjectsForPredicate(query1).flatMap { o =>
			getObjectsForSubjectAndPredicate(o, query2)
		}
	}

	def getObjectsForPredicate(predicate: String) : List[String] = {
		val s = g.edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}

	def getObjectsForSubjectAndPredicate(subject: String, predicate: String) : List[String] = {
		val s = g.edges.filter { edge =>
			edge.source.toString() == subject &&
				edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}

	def getTriplesForSubjectAndPredicate(subject: String, predicate: String) : List[RdfTriple] = {
		val s = g.edges.filter { edge =>
			edge.source.toString() == subject &&
				edge.label.toString.contains(predicate)
		}

		s.flatMap { edge =>
			getTriplesForSubject(edge.target.toString())
		}.toList
	}

	def getTriplesForSubject(subject: String) : List[RdfTriple] = {
		val s = g.edges.filter { edge =>
			edge.source.toString() == subject
		}

		s.map { edge =>
			RdfTriple(RdfResource(edge.source.toString), RdfResource(edge.label.toString), RdfResource(edge.target.toString))
		}.toList
	}
}
