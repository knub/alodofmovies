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
		val tripleGraph = new TripleGraph(triples)
		merge(tripleGraph)
	}

	def findCandidateMovies(g: TripleGraph): List[ResourceWithName] = {
//		val years = g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease").map { yearString =>
//			val split = yearString.split("-")
//			split(0).replace("\"", "").toInt
//		}.distinct
		val moviesInYear = List()//years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }

		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieNames = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			val l = currentMovieNames.map { movieName =>
				val l = StringUtils.getLevenshteinDistance(movieWithName.name, movieName)
//				println(f"$l, M1: #${movieWithName.name}#, M2: #$movieName#")
				l
			}.min
			l < 10
		}
		(moviesInYear ::: moviesWithSimilarName).distinct
	}

	def merge(triples: TripleGraph): Unit = {
		val candidates = findCandidateMovies(triples)
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val score = calculateActorOverlap(triples, candidate.resource) //TODO moviename
			movieScores += (candidate.resource -> score)
			if (i % 100 == 0)
				println(s"$i/${candidates.size}")
		}
		println("The best movies are:")
		movieScores.toList.sortBy { case (movie, score) => -score }.take(5).foreach(println)
	}

	def calculateActorOverlap(g: TripleGraph, candidateUri:String): Double = {
		val threshhold = 5
		val movieActors = g.getObjectsFor("dbpprop:starring", "rdfs:label")
		val candidateActors = Queries.getAllActorsOfMovie(candidateUri)

		if (candidateActors.isEmpty)
			return 0.0
		
		val matched_actors = movieActors.flatMap { actor =>
			val best_match = candidateActors.map(c_actor => StringUtils.getLevenshteinDistance(c_actor.name, actor)).min
			if (best_match < threshhold) List(actor)
			else List()
		} 
		matched_actors.size.toDouble / movieActors.size
	}
}
