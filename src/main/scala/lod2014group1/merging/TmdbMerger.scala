package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TmdbMovieTriplifier
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph
import lod2014group1.database.{ResourceWithName, Queries}
import org.apache.commons.lang3.StringUtils

class TmdbMerger {

	val tmdbTriplifier = new TmdbMovieTriplifier
	val movieNames = Queries.getAllMovieNames

	def mergeForrestGump(): Unit = {
		val triples = tmdbTriplifier.triplify(new File("data/TMDBMoviesList/movie/13.json"))
		val edges = triples.map { triple =>
			LDiEdge(triple.s.toString(), triple.o.toString)(triple.p.toString())
		}
		val g = Graph(edges: _*)
		merge(g)
	}

	def findCandidateMovies(g: Graph[String, LDiEdge]): List[ResourceWithName] = {
		val years = getObjectsFor(g, "dbpprop:released", "dbpprop:initialRelease").map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }

		val movieResource = getObjectOfType(g, "dbpedia-owl:Film")
		println(movieResource)
		println(getObjectsForSubjectAndPredicate(g, movieResource, "rdfs:label"))
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			StringUtils.getLevenshteinDistance(movieWithName.name, "Forrest Gump") < 5
		}
		moviesInYear ::: moviesWithSimilarName
	}

	def merge(triples: Graph[String, LDiEdge]): Unit = {
		val candidates = findCandidateMovies(triples)
		var movieScores = Map[String, Double]()
		candidates.foreach { candidate =>
			val score = calculateActorOverlap()
			movieScores += (candidate.resource -> score)
		}
	}

	def calculateActorOverlap(): Double = {
		1.0
	}

	def getObjectOfType(g: Graph[String, LDiEdge], rdfType: String): String = {
		g.edges.find { edge =>
			println(edge.target.toString)
			edge.label.toString == "rdf:type" &&
			edge.target.toString == rdfType
		}//.get.source.toString
		"lod:TmdbMovie13"
	}

	def getObjectsFor(g: Graph[String, LDiEdge], query1: String, query2: String): List[String] = {
		getObjectsForPredicate(g, query1).flatMap { o =>
			getObjectsForSubjectAndPredicate(g, o, query2)
		}
	}

	def getObjectsForPredicate(g: Graph[String, LDiEdge], predicate: String) : List[String] = {
		val s = g.edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}
	def getObjectsForSubjectAndPredicate(g: Graph[String, LDiEdge], subject: String, predicate: String) : List[String] = {
		val s = g.edges.filter { edge =>
			edge.source.toString().contains(subject) &&
				edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}

}
