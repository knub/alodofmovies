package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.{Triplifier, TmdbMovieTriplifier}
import lod2014group1.database.{ResourceWithName, Queries}
import org.apache.commons.lang3.StringUtils
import scala.pickling._
import json._
import org.apache.commons.io.{FileUtils, IOUtils}
import lod2014group1.rdf.RdfTriple
import scala.collection.JavaConversions._
import scala.util.Random

class MovieMatcher {

	val ACTOR_OVERLAP_MINIMUM       = 0.8
	val ACTOR_OVERLAP_LEVENSHTEIN   = 5
	val CANDIDATE_MOVIE_LEVENSHTEIN = 5
	val TEST_SET_SIZE               = 10

	val tmdbTriplifier = new TmdbMovieTriplifier
	val movieNames = Queries.getAllMovieNames
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

	def runStatistic(dir: File, triplifier: Triplifier): Unit = {
		var falseMatched = List[CandidateScore]()
		var trueMatched  = List[CandidateScore]()

		var noImdbId     = List[String]()
		var notMatched   = List[String]()

		val r = new Random(1000)
		val testSet =  r.shuffle(dir.listFiles().toList).take(TEST_SET_SIZE)
		testSet.foreach { file =>
			val triples = tmdbTriplifier.triplify(file)
			val tripleGraph = new TripleGraph(triples)
			val imdbMovie = merge(tripleGraph)
			val imdbId = getImdbId(tripleGraph)
			if (imdbId == null) {
				noImdbId ::= file.getName
				return
			}

			if (imdbMovie.isEmpty) {
				notMatched ::= file.getName
			} else {
				val bestMovie = imdbMovie.head
				if (getImdbId(bestMovie) == imdbId)
					trueMatched ::= bestMovie
				else
					falseMatched ::= bestMovie
			}
		}
		
		println(s"There were ${testSet.size} files.")
		println(s"${trueMatched.size} were matched correctly.")
		println(s"${falseMatched.size} were matched incorrectly")
		println(s"${notMatched.size} were not matched at all.")
		println(s"${noImdbId.size} had no imdb id.")
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
		val years = g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease").map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		println(years)
		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }

		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieNames = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		println(s"Movie: ${currentMovieNames(0)}")
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			val l = currentMovieNames.map { movieName =>
				val l = StringUtils.getLevenshteinDistance(movieWithName.name, movieName)
//				println(f"$l, M1: #${movieWithName.name}#, M2: #$movieName#")
				l
			}.min
			l < CANDIDATE_MOVIE_LEVENSHTEIN
		}
		(moviesInYear ::: moviesWithSimilarName).distinct
	}

	case class CandidateScore(candidate: String, score: Double)


	def merge(triples: TripleGraph): List[CandidateScore] = {
		val candidates = findCandidateMovies(triples)
		println(s"Found ${candidates.size} candidates.")
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val score = calculateActorOverlap(triples, candidate.resource)
			movieScores += (candidate.resource -> score)
//			if (i % 1000 == 0)
//				println(s"$i/${candidates.size}")
		}
		val bestMovies = movieScores.toList.map(CandidateScore.tupled).sortBy(-_.score).take(5)
		if (bestMovies.isEmpty)
			return List()
		if (bestMovies(0).score < ACTOR_OVERLAP_MINIMUM) {
			bestMovies.foreach(println)
		}
		bestMovies.filter { _.score > ACTOR_OVERLAP_MINIMUM }
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
		similarObjects.size.toDouble / currentObjects.size
	}
}
