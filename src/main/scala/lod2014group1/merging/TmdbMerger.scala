package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TmdbMovieTriplifier
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph

class TmdbMerger {

	val tmdbTriplifier = new TmdbMovieTriplifier
	val triples = tmdbTriplifier.triplify(new File("data/TMDBMoviesList/movie/13.json"))
	val edges = triples.map { triple =>
		LDiEdge(triple.s, triple.o)(triple.p)
	}
	val g = Graph(edges: _*)

	def mergeTmdb(): Unit = {
		getObjectsFor("starring").foreach { o =>
			getObjectsFor("dbprop:name").foreach {
				println(_)
			}
		}

	}

	def getObjectsFor(predicate: String): List[String] = {
		val s = g.edges.filter { edge =>
			edge.label.toString.contains(predicate)
		}

		s.map { edge =>
			edge.target.toString()
		}.toList
	}

}
