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
		// TODO: Do not use only first
		val movieName = getObjectsForSubjectAndPredicate(g, movieResource, "dbpprop:name")(0)
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			StringUtils.getLevenshteinDistance(movieWithName.name, movieName) < 5
		}
		moviesInYear ::: moviesWithSimilarName
	}

	def merge(triples: Graph[String, LDiEdge]): Unit = {
		val candidates = findCandidateMovies(triples)
//		candidates.foreach(println)
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val score = calculateActorOverlap(triples, candidate.resource) //TODO moviename
			movieScores += (candidate.resource -> score)
			if (i % 100 == 0)
				println(s"$i/${candidates.size}")
		}
		println("The best movies are:")
		movieScores.toList.sortBy { case (movie, score) => -score }.take(5).foreach(println)
		val bestMovie = movieScores.maxBy { case (movie, score) => score }
	}

	def calculateActorOverlap(g: Graph[String, LDiEdge], candidateUri:String): Double = {
		val threshhold = 5
//		println("=========================")
//		println("==movie")
		val movieActors = getObjectsFor(g, "dbpprop:starring", "rdfs:label")
//		println(movieActors)
//		println("==candidates")
		val candidateActors = Queries.getAllActorsOfMovie(candidateUri)
//		println(candidateUri)
//		candidateActors.foreach(a =>  println(a.name))
		
		if (candidateActors.isEmpty)
			return 0.0;
		
		val matched_actors = movieActors.flatMap { actor =>
			val best_match = candidateActors.map(c_actor => StringUtils.getLevenshteinDistance(c_actor.name, actor)).min
			if (best_match < threshhold) List(actor)
			else List()
		} 
//		println("==match")
//		println(matched_actors)
		matched_actors.size.toDouble / movieActors.size
	}

	def getObjectOfType(g: Graph[String, LDiEdge], rdfType: String): String = {
		g.edges.find { edge =>
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
			edge.source.toString() == subject &&
				edge.label.toString.contains(predicate)
		}
		s.map { edge =>
			edge.target.toString()
		}.toList
	}

}
