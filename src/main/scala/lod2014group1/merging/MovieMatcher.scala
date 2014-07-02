package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.{Triplifier, TmdbMovieTriplifier}
import lod2014group1.database._
import org.apache.commons.lang3.StringUtils
import scala.pickling._
import json._
import org.apache.commons.io.FileUtils
import lod2014group1.rdf.UriBuilder
import scala.util.Random
import lod2014group1.database.ResourceWithName
import lod2014group1.rdf.RdfTriple
import java.text.Normalizer

class MovieMatcher(val triplifier: Triplifier) {
	var ACTOR_OVERLAP_MINIMUM       = 0.8
	var ACTOR_OVERLAP_LEVENSHTEIN   = 3
	var CANDIDATE_MOVIE_LEVENSHTEIN = 5
	var CANDIDATE_SET_SIZE          = 100
	var TEST_SET_SIZE               = 750
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
						if (score > ACTOR_OVERLAP_MINIMUM) {
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

	def runStatistic(dir: File): Unit = {
		val r = new Random(1001)
		val testSet =  r.shuffle(dir.listFiles().toList.sortBy(_.getName)).take(TEST_SET_SIZE)
		testSet.zipWithIndex.foreach(mergeMovie)

		log(f"${testSet.size}%4s files: ")
		log(f"${trueMatched.size}%4s were matched correctly.")
		log(f"${falseMatched.size}%4s were matched incorrectly:")
		falseMatched.foreach(log)
		log(f"${notInCandidate.size}%4s were not matched and are not candidates:")
		notInCandidate.foreach(log)
		log(f"${noCandidates.size}%4s were not matched and no candidates were found:")
		noCandidates.foreach(log)
		log(f"${notMatched.size}%4s were not matched for unknown reasons:")
		notMatched.foreach(log)
		log(f"${notInDb.size}%4s were not matched because we do not have it in our database.")
		log(f"${noImdbId.size}%4s had no imdb id.")
		log()
		log("Precision = matched correctly/(correctly + incorrectly)")
		val precision = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size)
		println(s"Precision    = $precision")
		log("Recall = matched correctly/(correctly + incorrectly + no candidates + not in candidates + unknown reasons)")
		val recall = trueMatched.size.toDouble / (trueMatched.size + falseMatched.size + noCandidates.size + notInCandidate.size + notMatched.size)
		println(s"Recall       = $recall")
		log("F1-measure = (2 * Precision * Recall) / (Precision + Recall)")
		val f1Measure = (2 * precision * recall) / (precision + recall)
		println(s"F1-measure   = $f1Measure")

		log("F0.5-measure = (1.25 * Precision * Recall) / (0.25 * Precision + Recall)")
		val f05Measure = (1.25 * precision * recall) / (0.25 * precision + recall)
		println(s"F0.5-measure = $f05Measure")
	}

	def mergeMovie(t: (File, Int)): Unit = {
		val (file, i) = t
		val fileId = file.getName.replace(".json", "")

		val triples = triplifier.triplify(FileUtils.readFileToString(file))
		val tripleGraph = new TripleGraph(triples)
		val imdbId = tripleGraph.getImdbId
		if (imdbId == null) {
			log("No IMDB ID. Skip.")
			noImdbId ::= fileId
			return
		}
		if (!taskDb.hasTasks(imdbId)) {
			log("Not in DB. Skip.")
			notInDb ::= fileId
			return
		}

		log(i)
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
			Merger.mergeMovieTriple(imdbMovie.head.candidate, movietriple).foreach(log)
			// TODO: Try other methods
		}
	}

	def extractAllMovieNamesFromGraph(g: TripleGraph): List[String] = {
		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieOriginalTitles = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:originalTitle")
		val currentMovieTitles = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		(currentMovieTitles ::: currentMovieOriginalTitles).distinct
	}

	def extractMovieNamesFromResource(movieWithName: ResourceWithNameAndOriginalTitle): List[String] = {
		List(movieWithName.name, movieWithName.originalTitle).filter(_ != null).distinct
	}

	def findCandidateMovies(g: TripleGraph): List[ResourceWithNameAndOriginalTitle] = {
		val currentMovieNames = extractAllMovieNamesFromGraph(g)

		log(s"========== Movie: ${currentMovieNames(0)} ==========")
		val moviesWithSimilarName = movieNames.map { movieWithName =>
			val names = extractMovieNamesFromResource(movieWithName)
			val l = names.map { name =>
				currentMovieNames.map { movieName =>
					StringUtils.getLevenshteinDistance(name, movieName)
				}.min
			}.min
			(movieWithName, l)
		}

		moviesWithSimilarName.filter(_._2 < CANDIDATE_MOVIE_LEVENSHTEIN).sortBy(_._2).take(CANDIDATE_SET_SIZE).map(_._1)

//		val years = (g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease")::: g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:initialRelease")).map { yearString =>
//			val split = yearString.split("-")
//			split(0).replace("\"", "").toInt
//		}.distinct
//		if (years.isEmpty)
//			log("No years found.")
//		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }
//		val allCandidates = (moviesInYear ::: moviesWithSimilarName).distinct
//		val allCandidates = moviesWithSimilarName.distinct

//		allCandidates
	}

	case class CandidateScore(candidate: String, score: Double)

	def merge(triples: TripleGraph): List[CandidateScore] = {
		val candidates = findCandidateMovies(triples)
		log(s"Found ${candidates.size} candidates.")
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val scoringFunctions: List[(TripleGraph, ResourceWithNameAndOriginalTitle) => Double] = List(
				calculateActorOverlap,
				calculateDirectorOverlap,
				nameSimilarity
			)
			val weights = List(20, 20, 1)

			val scoringWeights = scoringFunctions.zip(weights)
			val score = avg(scoringWeights.map { case (scorer, weight) =>
				(scorer(triples, candidate), weight)
			}.filter { case (partScore, weight)  =>
				partScore != -1.0
			})
			movieScores += (candidate.resource -> score)
		}
		val bestMovies = movieScores.toList.map(CandidateScore.tupled).sortBy(-_.score)
		if (bestMovies.isEmpty)
			return List()
		if (bestMovies(0).score < ACTOR_OVERLAP_MINIMUM) {
			log("Could not find a single match. Here are the best five matches:")
			bestMovies.take(5).foreach { movie =>
				log(movie.candidate.replace("http://purl.org/hpi/movie#Movie", "www.imdb.com/title/") + " with score "  + movie.score)
			}
		}
		bestMovies
	}

	def avg(l: List[(Double, Int)]): Double = {
		if (l.isEmpty)
			0.0
		val weightSum = l.map(_._2).sum
		l.map { case (score, weight) =>
			score * weight
		}.sum / weightSum.toDouble
	}

	def nameSimilarity(g: TripleGraph, candidate: ResourceWithNameAndOriginalTitle): Double = {
		val externalNames = extractAllMovieNamesFromGraph(g)
		val imdbNames     = extractMovieNamesFromResource(candidate)

		val cross = for { x <- externalNames; y <- imdbNames } yield (x, y)
		val matchExists = cross.exists { case (name1, name2) =>
			Normalizer.normalize(name1, Normalizer.Form.NFD) == Normalizer.normalize(name2, Normalizer.Form.NFD)
		}
		if (matchExists)
			1.0
		else
			0.0
	}
	def calculateActorOverlap(g: TripleGraph, candidate: ResourceWithNameAndOriginalTitle): Double = {
		val currentActors    = g.getObjectsFor("dbpprop:starring", "rdfs:label")

		val cacheFile = new File(s"data/MergeMovieActor/${candidate.resource.split("#")(1)}")
		val candidateActors = if (cacheFile.exists()) {
			val json = FileUtils.readFileToString(cacheFile, "UTF-8")
			json.unpickle[List[ResourceWithName]]
		}
		else {
			val tmp = Queries.getAllActorsOfMovie(candidate.resource)
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

	def calculateDirectorOverlap(g: TripleGraph, candidate: ResourceWithNameAndOriginalTitle): Double = {
		val currentDirectors = g.getObjectsFor("dbpprop:director", "dbpprop:name")
		val canidateDirectors = Queries.getAllDirectorsOfMovie(candidate.resource)

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
				StringUtils.getLevenshteinDistance(sortName(canidateObject.name), sortName(currentObject))
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

	def sortName(name: String): String = {
		name.split(" ").sorted.mkString(" ")
	}
}
