package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TMDBFilmsTriplifier
import scalax.collection.edge.LDiEdge
import scalax.collection.Graph

class TmdbMerger {

	def mergeTmdb(): Unit = {
		val tmdbTriplifier = new TMDBFilmsTriplifier
		val triples = tmdbTriplifier.triplify(new File("data/TMDBMoviesList/movie/13.json"))

		val edges = triples.map { triple =>
			LDiEdge(triple.s, triple.o)(triple.p)
		}

		val g = Graph(edges: _*)

		val s = g.edges.filter { edge =>
			edge.label.toString.contains("starring")
		}

		s.foreach { edge =>
			println(edge.target)
		}
	}

}
