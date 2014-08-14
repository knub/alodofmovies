package lod2014group1.merging

import java.io.File
import java.text.Normalizer
import java.util.Date

import lod2014group1.database.{ResourceWithName, _}
import lod2014group1.rdf.{RdfTriple, UriBuilder}
import lod2014group1.triplification.{TmdbMovieTriplifier, Triplifier}
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import scala.pickling._
import scala.pickling.json._
import scala.util.Random

class DirectMovieMatcher(val triplifier: Triplifier) {
	var SCORE_THRESHOLD             = 0.3
	var CANDIDATE_MOVIE_LEVENSHTEIN = 0
	var TEST_SET_SIZE               = 750
	var RANDOM                      = 1001
	var VERBOSE                     = true


	def log(): Unit = {}
	def log(s: Any): Unit = {
		if (VERBOSE)
			println(s.toString)
	}
	val tmdbTriplifier = new TmdbMovieTriplifier
	val movieNames = Queries.getAllMoviesWithNameAndOriginalTitles
	val taskDb = new TaskDatabase()
	new File(s"data/MergeMovieActor/").mkdir()

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
						if (minScore(score)) {
							f"$originUri%45s has top score: $score%.3f and matched correctly to $correctUri"
						} else {
							f"$originUri%45s has low top score: $score%.3f but would be matched correctly to $correctUri"
						}
					} else {
						val matchedUri = UriBuilder.getImdbMovieUri(matched)
						f"$originUri%45s was matched with top score: $score%.3f to $matchedUri should be $correctUri"
					}
				}
			} else {
				if (matched != null) {
					val matchedUri = UriBuilder.getImdbMovieUri(matched)
					f"$originUri%45s matched with top score: $score%.3f to $matchedUri correct movie unknown"
				} else {
					f"$originUri%45s was not matched and correct movie unknown"
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
	var wronglyMatchedNotInDbSum = 0

	def runStatistic(dir: File): Unit = {
		val r = new Random(RANDOM)
//		val filter = { a: File => a.getName == "13.json" }
		val filter = { a: File => true }
		val testSet =  r.shuffle(dir.listFiles().toList.filter(filter).sortBy(_.getName)).take(TEST_SET_SIZE)
		try {
			testSet.zipWithIndex.foreach(mergeMovie)
		} catch {
			case e: Throwable =>
				println("Early abort because of:")
				println(e.printStackTrace())
				println(e)
		}

		log(f"${testSet.size}%4s files: ")
		log(f"${trueMatched.size}%4s were matched correctly.")
		log(f"${falseMatched.size + wronglyMatchedNotInDbSum}%4s were matched incorrectly:")
		log(f"${falseMatched.size}%4s were matched incorrectly and are in db:")
		falseMatched.foreach(log)
		log(f"${notInCandidate.size}%4s were not matched and are not candidates:")
		notInCandidate.foreach(log)
		log(f"${noCandidates.size}%4s were not matched and no candidates were found:")
		noCandidates.foreach(log)
		log(f"${notMatched.size}%4s were not matched for unknown reasons:")
		notMatched.foreach(log)
		log(f"${notInDb.size - wronglyMatchedNotInDbSum}%4s were correctly not matched because we do not have it in our database.")
		log(f"${notInDb.size}%4s are not in our database.")
		log(f"${noImdbId.size}%4s had no imdb id.")
		log()
		log("Precision = matched correctly/(correctly + incorrectly)")
		val precision = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size + wronglyMatchedNotInDbSum)
		log(s"Precision    = $precision")
		log("Recall = matched correctly/(correctly + incorrectly + no candidates + not in candidates + unknown reasons)")
		val recall = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size + noCandidates.size + notInCandidate.size + notMatched.size)
		log(s"Recall       = $recall")
		log("F1-measure = (2 * Precision * Recall) / (Precision + Recall)")
		val f1Measure = (2 * precision * recall) / (precision + recall)
		log(s"F1-measure   = $f1Measure")

		log("F0.5-measure = (1.25 * Precision * Recall) / (0.25 * Precision + Recall)")
		val f05Measure = (1.25 * precision * recall) / (0.25 * precision + recall)
		println(s"baseline,$precision,$recall,$f1Measure,$f05Measure")
		log(s"F0.5-measure = $f05Measure")

	}

	def mergeMovie(t: (File, Int)): Unit = {
		val (file, i) = t

		if (i % 1000 == 0)
			log(f"$i%5s " + new Date().toString)
		val fileId = file.getName.replace(".json", "")

		val triples = triplifier.triplify(FileUtils.readFileToString(file))
		val tripleGraph = new TripleGraph(triples)
		val imdbId = tripleGraph.getImdbId()
		if (imdbId == null) {
//			log("No IMDB ID. Skip.")
			noImdbId ::= fileId
			return
		}
		val isInDb = taskDb.hasTasks(imdbId)
		if (!isInDb) {
//			log("Not in DB. Skip.")
			notInDb ::= fileId
			wronglyMatchedNotInDbSum += merge(tripleGraph).size
			return
		}
		val candidates = merge(tripleGraph)
		val candidateIds = candidates.map { c => getImdbId(c) }

		if (candidates.size == 0) {
			noCandidates ::= new ResultIds(fileId, -1.0, null, imdbId)
		} else {
			candidates.foreach { candidate =>
				val bestMovieImdbId = getImdbId(candidate)
				if (bestMovieImdbId == imdbId) {
					trueMatched ::= new ResultIds(fileId, candidate.score, bestMovieImdbId, imdbId)
				} else
					falseMatched ::= new ResultIds(fileId, candidate.score, bestMovieImdbId, imdbId)
			}

		}
	}

	def minScore(cs: CandidateScore): Boolean = {
		minScore(cs.score)
	}

	def minScore(score: Double): Boolean = {
		score > SCORE_THRESHOLD
	}

	def extractAllMovieNamesFromGraph(g: TripleGraph): List[String] = {
		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieOriginalTitles = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:originalTitle")
		val currentMovieTitles = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		(currentMovieTitles ::: currentMovieOriginalTitles).distinct
	}

	def extractMovieNamesFromResource(movieWithName: ResourceWithNameAndOriginalTitleAndYear): List[String] = {
		List(movieWithName.name, movieWithName.originalTitle).filter(_ != null).distinct
	}

	def findCandidateMovies(g: TripleGraph): List[ResourceWithNameAndOriginalTitleAndYear] = {
		val currentMovieNames = extractAllMovieNamesFromGraph(g)

		val moviesWithSimilarName = movieNames.map { movieWithName =>
			val names = extractMovieNamesFromResource(movieWithName)
			val l = names.map { name =>
				val normalizedName = Normalizer.normalize(name, Normalizer.Form.NFD)
				currentMovieNames.map { movieName =>
					StringUtils.getLevenshteinDistance(normalizedName, Normalizer.normalize(movieName, Normalizer.Form.NFD))
				}.min
			}.min
			(movieWithName, l)
		}

		moviesWithSimilarName.filter(_._2 <= CANDIDATE_MOVIE_LEVENSHTEIN).map(_._1)
	}

	case class CandidateScore(candidate: String, score: Double)

	def merge(triples: TripleGraph): List[CandidateScore] = {
		val candidates = findCandidateMovies(triples)

		val matchingMovies = candidates.map { candidate =>
			CandidateScore(candidate.resource, 1.0)
		}

		matchingMovies
	}

	def nameAndYearSimilarity(g: TripleGraph, candidate: ResourceWithNameAndOriginalTitleAndYear): Double = {
		val externalNames = extractAllMovieNamesFromGraph(g)
		val imdbNames     = extractMovieNamesFromResource(candidate)

		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val years = (g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease")::: g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:initialRelease")).map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		val yearScore = if (years.contains(candidate.year)) 1.0 else 0.0
		val cross = for { x <- externalNames; y <- imdbNames } yield (x, y)
		val nameScore = if (cross.exists { case (name1, name2) =>
			Normalizer.normalize(name1, Normalizer.Form.NFD) == Normalizer.normalize(name2, Normalizer.Form.NFD)
		}) 1.0 else 0.0
		val sum = List(yearScore, nameScore).sum
		if (sum == 1.0)
			SCORE_THRESHOLD
		else
			sum / 2.0
	}

}
