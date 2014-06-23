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
		val triples = tmdbTriplifier.triplify(new File("data/TMDBMoviesList/movie/9502.json"))
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

	def calculateActorOverlap(g: TripleGraph, candidateUri: String): Double = {
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

	def calculateProducerOverlap(g: TripleGraph, candidateUri: String): Double = {
		val threshhold = 5
		val movieProducers = g.getObjectsFor("dbpprop:producer", "dbpprop:name") ::: g.getObjectsFor("dbpprop:coProducer", "dbpprop:name")
		val canidateProducers = Queries.getAllProducersOfMovie(candidateUri)

		if (canidateProducers.isEmpty)
			return 0.0

		val matchedProducers = movieProducers.flatMap { producer =>
			val best_match = canidateProducers.map(cProducer => StringUtils.getLevenshteinDistance(cProducer.name, producer)).min
			if (best_match < threshhold) List(producer)
			else List()
		}
		matchedProducers.size.toDouble / movieProducers.size
	}

	def calculateDirectorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val threshhold = 5
		val movieDirectors = g.getObjectsFor("dbpprop:director", "dbpprop:name")
		val canidateDirectors = Queries.getAllDirectorsOfMovie(candidateUri)

		if (canidateDirectors.isEmpty)
			return 0.0

		val matchedDirectors = movieDirectors.flatMap { director =>
			val best_match = canidateDirectors.map(cDirector => StringUtils.getLevenshteinDistance(cDirector.name, director)).min
			if (best_match < threshhold) List(director)
			else List()
		}
		matchedDirectors.size.toDouble / movieDirectors.size
	}
}
