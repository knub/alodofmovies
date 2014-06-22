package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TmdbMovieTriplifier
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph

class TmdbMerger {

	val tmdbTriplifier = new TmdbMovieTriplifier

	def mergeForrestGump(): Unit = {
		val triples = tmdbTriplifier.triplify(new File("data/TMDBMoviesList/movie/13.json"))
		val edges = triples.map { triple =>
			LDiEdge(triple.s.toString(), triple.o.toString)(triple.p.toString())
		}
		val g = Graph(edges: _*)
		merge(g)
	}

	def findCandidateMovies(graph: Graph[String, LDiEdge]): List[String] = {
		List()
	}

	def merge(triples: Graph[String, LDiEdge]): Unit = {
		val candidates = findCandidateMovies(triples)
		getObjectsFor(triples, "starring").foreach { o =>
			getObjectsFor(triples, "dbprop:name").foreach {
				println(_)
			}
		}

	}

	def getObjectsFor(g: Graph[String, LDiEdge], predicate: String): List[String] = {
		val s = g.edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}

		s.map { edge =>
			edge.target.toString()
		}.toList
	}

}
