package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TmdbMovieTriplifier
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph
import lod2014group1.database.{MovieWithName, Queries}
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

	def findCandidateMovies(g: Graph[String, LDiEdge]): List[MovieWithName] = {
		// TODO: Extract
		val moviesInYear = Queries.getAllMovieNamesOfYear("1994")
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			// TODO: Extract
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

	def getObjectsFor(g: Graph[String, LDiEdge], predicate: String): List[String] = {
		g.edges.foreach(println)
		val s = g.edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}

}
