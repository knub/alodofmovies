package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.{Triplifier, TmdbMovieTriplifier}
import lod2014group1.database.{TaskDatabase, ResourceWithName, Queries}
import org.apache.commons.lang3.StringUtils
import scala.pickling._
import json._
import org.apache.commons.io.{FileUtils, IOUtils}
import lod2014group1.rdf.{UriBuilder, RdfTriple}
import scala.util.Random
import scala.slick.lifted.Functions

class MovieMatcher(val triplifier: Triplifier) {
	// TODO use orginial_title
	// TODO use more to calc score (e.g. original_title distance)
	// TODO candidate movies more criteria -> calc score and take top 100
	// TODO find bugs (e.g. freebase)

	val ACTOR_OVERLAP_MINIMUM       = 0.8
	val ACTOR_OVERLAP_LEVENSHTEIN   = 3
	val CANDIDATE_MOVIE_LEVENSHTEIN = 5
	val TEST_SET_SIZE               = 750


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

	case class ResultIds(origin: String, score: Double, matched: String, correct: String) {
		override def toString: String = {
			val originUri = if (origin forall Character.isDigit) {
				UriBuilder.getTmdbMovieUri(origin)
			}	else {
				UriBuilder.getFreebaseUri(s"/m/$origin")
			}
			if (correct != null){
				val correctUri = UriBuilder.getImdbMovieUri(correct)
				if (score == -1.0 || matched == null) {
					f"$originUri%45s was not matched but should be $correctUri"
				} else {
					if (matched.equals(correct)) {
						f"$originUri%45s matched with top score: $score%.3f correclty to $correctUri"
					} else {
						val matchedUri = UriBuilder.getImdbMovieUri(matched)
						f"$originUri%45s matched with top score: $score%.3f to $matchedUri should be $correctUri"
					}
				}
			} else {
				if (matched != null) {
					val matchedUri = UriBuilder.getImdbMovieUri(matched)
					f"$originUri%45s matched with top score: $score%.3f to $matchedUri correct movie unknown"
				} else {
					f"$originUri%45s was not matched"
				}
			}
		}
	}

	var falseMatched    = List[ResultIds]()
	var trueMatched     = List[ResultIds]()
	var notInCandidate  = List[ResultIds]()
	var noCandidates    = List[ResultIds]()
	var notMatched      = List[ResultIds]()
	var noImdbId        = List[String]()
	var notInDb         = List[String]()

	def runStatistic(dir: File): Unit = {
		val r = new Random(1001)
		val testSet =  r.shuffle(dir.listFiles().toList.sortBy(_.getName)).take(TEST_SET_SIZE)
		testSet.zipWithIndex.foreach(mergeMovie)

		println(f"${testSet.size}%4s files: ")
		println(f"${trueMatched.size}%4s were matched correctly.")
		println(f"${falseMatched.size}%4s were matched incorrectly:")
		falseMatched.foreach(println)
		println(f"${notInCandidate.size}%4s were not matched and are not candidates:")
		notInCandidate.foreach(println)
		println(f"${noCandidates.size}%4s were not matched and no candidates were found:")
		noCandidates.foreach(println)
		println(f"${notMatched.size}%4s were not matched for unknown reasons:")
		notMatched.foreach(println)
		println(f"${notInDb.size}%4s were not matched because we do not have it in our database.")
		println(f"${noImdbId.size}%4s had no imdb id.")
		println()
		println("Precision = matched correctly/(correctly + incorrectly)")
		val precision = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size)
		println(s"Precision = $precision")
		println("Recall = matched correctly/(correctly + incorrectly + no candidates + not in candidates + unknown reasons)")
		val recall = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size + noCandidates.size + notInCandidate.size + notMatched.size)
		println(s"Recall = $recall")
		println("F-measure = (2 * Precision * Recall) / (Precision + Recall)")
		val fMeasure = (2 * precision * recall) / (precision + recall)
		println(s"F-measure = $fMeasure")
	}

	def mergeMovie(t: (File, Int)): Unit = {
		val (file, i) = t
		val fileId = file.getName.replace(".json", "")

		val triples = triplifier.triplify(FileUtils.readFileToString(file))
		val tripleGraph = new TripleGraph(triples)
		val imdbId = getImdbId(tripleGraph)
		if (imdbId == null) {
			println("No IMDB ID. Skip.")
			noImdbId ::= fileId
			return
		}
		if (!taskDb.hasTasks(imdbId)) {
			println("Not in DB. Skip.")
			notInDb ::= fileId
			return
		}

		println(i)
		val candidates = merge(tripleGraph)
		val candidateIds = candidates.map { c => getImdbId(c) }

		if (candidates.size == 0) {
			noCandidates ::= new ResultIds(fileId, -1.0, null, imdbId)
		} else {
			val bestMovie = candidates.head
			val bestMovieImdbId = getImdbId(bestMovie)

			if (candidates.filter(minScore).isEmpty) {
				if (!candidateIds.exists( c => c.equals(imdbId))) {
					notInCandidate ::= new ResultIds(fileId, bestMovie.score, bestMovieImdbId, imdbId)
				} else {
					notMatched ::= new ResultIds(fileId, bestMovie.score, bestMovieImdbId, imdbId)
				}
			} else {
				if (bestMovieImdbId == imdbId)
					trueMatched ::= new ResultIds(fileId, bestMovie.score, bestMovieImdbId, imdbId)
				else
					falseMatched ::= new ResultIds(fileId, bestMovie.score, bestMovieImdbId, imdbId)
			}
		}
	}

	def minScore(cs: CandidateScore): Boolean = {
		cs.score > ACTOR_OVERLAP_MINIMUM
	}
	
	def mergeTmdbMovie(file: File): Unit = {
		val triples = tmdbTriplifier.triplify(file)
		mergeTriples(triples)
	}
	
	def mergeTriples(triples: List[RdfTriple]): Unit = {
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

		val years = (g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease")::: g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:initialRelease")).map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		if (years.isEmpty)
			println("No years found.")
		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }

		val allCandidates = (moviesInYear ::: moviesWithSimilarName).distinct
//		val allCandidates = moviesWithSimilarName.distinct

		allCandidates
	}

	case class CandidateScore(candidate: String, score: Double)

	def merge(triples: TripleGraph): List[CandidateScore] = {
		val candidates = findCandidateMovies(triples)
		println(s"Found ${candidates.size} candidates.")
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val overlaps: List[(TripleGraph, String) => Double] = List(calculateActorOverlap, calculateDirectorOverlap)
			val score = avg(overlaps.map(_(triples, candidate.resource)).filter(_ != -1.0))
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

	def avg(l: List[Double]): Double = {
		l.sum / l.size.toDouble
	}

	def calculateActorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentActors    = g.getObjectsFor("dbpprop:starring", "rdfs:label")

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

	def calculateWriterOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentWriters = g.getObjectsFor("dbpprop:writer", "dbpprop:name")
		val candidateWriters = Queries.getAllWritersOfMovie(candidateUri)

		calculateOverlap(currentWriters, candidateWriters)
	}

	private def calculateOverlap(currentObjects: List[String], candidateObjects: List[ResourceWithName]): Double = {
		if (candidateObjects.isEmpty || currentObjects.isEmpty)
			return -1.0

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
