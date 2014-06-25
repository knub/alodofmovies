package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.{Triplifier, TmdbMovieTriplifier}
import lod2014group1.database.{TaskDatabase, ResourceWithName, Queries}
import org.apache.commons.lang3.StringUtils
import scala.pickling._
import json._
import org.apache.commons.io.{FileUtils, IOUtils}
import lod2014group1.rdf.RdfTriple
import scala.util.Random

class MovieMatcher {

	val ACTOR_OVERLAP_MINIMUM       = 0.8
	val ACTOR_OVERLAP_LEVENSHTEIN   = 5
	val CANDIDATE_MOVIE_LEVENSHTEIN = 5
	val TEST_SET_SIZE               = 10

	val tmdbTriplifier = new TmdbMovieTriplifier
	val movieNames = Queries.getAllMovieNames
	val taskDb = new TaskDatabase()
	new File(s"data/MergeMovieActor/").mkdir()

	def getImdbId(g: TripleGraph): String = {
		val sameAsTriples = g.getObjectsForPredicate("owl:sameAs").filter(p => p.contains("http://imdb.com/title/"))
		if (sameAsTriples.isEmpty)
			return null
		sameAsTriples.head.split("/").last.split(">")(0)
	}

	def getImdbId(cs: CandidateScore): String = {
		cs.candidate.split("Movie").last
	}

	case class ResultIds(score: Double, origin: String, matched: String, correct: String)

	def runStatistic(dir: File, triplifier: Triplifier): Unit = {
		var falseMatched = List[ResultIds]()
		var trueMatched  = List[ResultIds]()

		var notInDb      = List[ResultIds]()
		var noCandidate  = List[ResultIds]()
		var notMatched   = List[ResultIds]()

		var noImdbId     = List[String]()

		val r = new Random(1001)
		val testSet =  r.shuffle(dir.listFiles().toList.sortBy(_.getName)).take(TEST_SET_SIZE)
		testSet.foreach { file =>
			val triples = triplifier.triplify(FileUtils.readFileToString(file))
			val tripleGraph = new TripleGraph(triples)

			val candidates = merge(tripleGraph)
			val candidateIds = candidates.map { c => getImdbId(c) }
			val bestMovie = candidates.head
			val bestMovieImdbId = getImdbId(bestMovie)
			val imdbId = getImdbId(tripleGraph)

			if (imdbId == null) {
				noImdbId ::= file.getName
			} else {
				if (candidates.filter(minScore).isEmpty) {
					if (taskDb.hasTasks(imdbId)) {
						notInDb ::= new ResultIds(bestMovie.score, file.getName, bestMovieImdbId, imdbId)
					} else if (!candidateIds.exists( c => c.equals(imdbId))) {
						noCandidate ::= new ResultIds(bestMovie.score, file.getName, bestMovieImdbId, imdbId)
					} else {
						notMatched ::= new ResultIds(bestMovie.score, file.getName, bestMovieImdbId, imdbId)
					}
				} else {
					if (bestMovieImdbId == imdbId)
						trueMatched ::= new ResultIds(bestMovie.score, file.getName, bestMovieImdbId, imdbId)
					else
						falseMatched ::= new ResultIds(bestMovie.score, file.getName, bestMovieImdbId, imdbId)
				}
			}
		}

		println(s"There were ${testSet.size} files.")
		println(s"${trueMatched.size} were matched correctly.")
		println(s"${falseMatched.size} were matched incorrectly: ${falseMatched}.")
		println(s"${notInDb.size} were not matched because we do not have it in our database.")
		println(s"${noCandidate.size} were not matched and are not candidates: ${noCandidate}.")
		println(s"${notMatched.size} were not matched for unknown reasons: ${notMatched}.")
		println(s"${noImdbId.size} had no imdb id.")
		println()
		println("Precision = matched correctly/(test set size - not in database - no imdb id)")
		val precision = trueMatched.size.toDouble / (testSet.size - notInDb.size - noImdbId.size)
		println(s"Precision = $precision")
	}

	def minScore(cs: CandidateScore): Boolean = {
		cs.score > ACTOR_OVERLAP_MINIMUM
	}
	
	def mergeTmdbMovie(file: File): Unit = {
		val triples = tmdbTriplifier.triplify(file)
		getMergedTriple(triples)
	}
	
	def getMergedTriple(triples: List[RdfTriple]): Unit = {
		val tripleGraph = new TripleGraph(triples)
		val imdbMovie = merge(tripleGraph)
		if (!imdbMovie.isEmpty){	
			val movieResource = tripleGraph.getObjectOfType("dbpedia-owl:Film")
			val movietriple = tripleGraph.getTriplesForSubject(movieResource)
			Merger.mergeMovieTriple(imdbMovie.head.candidate, movietriple).foreach(println)
			// TODO: Try other methods
		}
		
	}


	def findCandidateMovies(g: TripleGraph): List[ResourceWithName] = {
		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieNames = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		println(s"========== Movie: ${currentMovieNames(0)} ==========")
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			val l = currentMovieNames.map { movieName =>
				val l = StringUtils.getLevenshteinDistance(movieWithName.name, movieName)
				l
			}.min
			l < CANDIDATE_MOVIE_LEVENSHTEIN
		}

		val years = g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease").map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		if (years.isEmpty)
			println("No years found.")
		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }
		val allCandidates = (moviesInYear ::: moviesWithSimilarName).distinct
			
		allCandidates
	}

	case class CandidateScore(candidate: String, score: Double)


	def merge(triples: TripleGraph): List[CandidateScore] = {
		val candidates = findCandidateMovies(triples)
		println(s"Found ${candidates.size} candidates.")
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val score = calculateActorOverlap(triples, candidate.resource)
			movieScores += (candidate.resource -> score)
		}
		val bestMovies = movieScores.toList.map(CandidateScore.tupled).sortBy(-_.score)
		if (bestMovies.isEmpty)
			return List()
		if (bestMovies(0).score < ACTOR_OVERLAP_MINIMUM) {
			println("Could not find a single match. Here are the best five matches:")
			bestMovies.take(5).foreach { movie =>
				println(movie.candidate.replace("http://purl.org/hpi/movie#Movie", "www.imdb.com/title/") + " with score "  + movie.score)
			}
		}
		bestMovies
	}

	def calculateActorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentActors = g.getObjectsFor("dbpprop:starring", "rdfs:label")
		val cacheFile = new File(s"data/MergeMovieActor/${candidateUri.split("#")(1)}")
		val candidateActors = if (cacheFile.exists()) {
			val json = FileUtils.readFileToString(cacheFile, "UTF-8")
			json.unpickle[List[ResourceWithName]]
		}
		else {
			val tmp = Queries.getAllActorsOfMovie(candidateUri)
			val jsonString = tmp.pickle.value
			FileUtils.writeStringToFile(cacheFile, jsonString, "UTF-8")
			tmp
		}

		calculateOverlap(currentActors, candidateActors)
	}

	def calculateProducerOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentProducers = g.getObjectsFor("dbpprop:producer", "dbpprop:name") ::: g.getObjectsFor("dbpprop:coProducer", "dbpprop:name")
		val candidateProducer = Queries.getAllProducersOfMovie(candidateUri)

		calculateOverlap(currentProducers, candidateProducer)
	}

	def calculateDirectorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentDirectors = g.getObjectsFor("dbpprop:director", "dbpprop:name")
		val canidateDirectors = Queries.getAllDirectorsOfMovie(candidateUri)

		calculateOverlap(currentDirectors, canidateDirectors)
	}

	private def calculateOverlap(currentObjects: List[String], candidateObjects: List[ResourceWithName]): Double = {
		if (candidateObjects.isEmpty)
			return 0.0

		val similarObjects = currentObjects.flatMap { currentObject =>
			val bestMatch = candidateObjects.map { canidateObject =>
				StringUtils.getLevenshteinDistance(canidateObject.name, currentObject)
			}.min

			if (bestMatch < ACTOR_OVERLAP_LEVENSHTEIN)
				List(currentObject)
			else
				List()
		}
		if (currentObjects.isEmpty)
			0.0
		else
			similarObjects.size.toDouble / currentObjects.size
	}
}
