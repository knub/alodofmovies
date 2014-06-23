package lod2014group1.merging

import java.io.File
import lod2014group1.triplification.TmdbMovieTriplifier
import lod2014group1.database.{ResourceWithName, Queries}
import org.apache.commons.lang3.StringUtils
import scala.pickling._
import json._
import org.apache.commons.io.{FileUtils, IOUtils}
import lod2014group1.rdf.RdfTriple
import scala.collection.JavaConversions._
import scala.util.Random

class TmdbMerger {

	val tmdbTriplifier = new TmdbMovieTriplifier
	val movieNames = Queries.getAllMovieNames
	new File(s"data/MergeMovieActor/").mkdir()

	def mergeForrestGump(): Unit = {
	//	val file = new File("data/TMDBMoviesList/movie/9502.json")
		val file = new File("data/TMDBMoviesList/movie/13.json")
		mergeTmdbMovie(file)
	}

	def runStatistic(): Unit = {
		val dir = new File("data/TMDBMoviesList/movie/")
		var falseMatched = List[(String, Double)]()
		var matched      = List[(String, Double)]()
		var notMatched   = List[(String, Double)]()
		var newMatched   = List[(String, Double)]()
		
		val r = new Random(1000)
		r.shuffle(dir.listFiles().toList).take(10).foreach { file => 	
			val triples = tmdbTriplifier.triplify(file)
			val tripleGraph = new TripleGraph(triples)
			val imdbMovie = merge(tripleGraph)
			if (!imdbMovie.isEmpty) {
				val imdbUrls = tripleGraph.getObjectsForPredicate("owl:sameAs").filter(p => p.contains("http://imdb.com/title/"))
				if (imdbUrls.nonEmpty) {
					if (imdbMovie.head._1.split("Movie").last == imdbUrls.head.split("/").last.split(">")(0))
						matched ::= imdbMovie.head
					else
						falseMatched ::= imdbMovie.head
				}
				else newMatched ::= imdbMovie.head
			}
			else
				notMatched ::= imdbMovie.head
		}
		
		println()
		println("matched:" + matched)
		println("falseMatched" + falseMatched)
		println("newMatched" + newMatched)
		println("notMatched" + notMatched)
		
		println()		
		println("matched:" + matched.size)
		println("falseMatched" + falseMatched.size)
		println("newMatched" + newMatched.size)
		println("notMatched" + notMatched.size)
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
			Merger.mergeMovieTriple(imdbMovie.head._1, movietriple).foreach(println)
			//TODO other Methods
		}
		
	}

	def findCandidateMovies(g: TripleGraph): List[ResourceWithName] = {
		val years = g.getObjectsFor("dbpprop:released", "dbpprop:initialRelease").map { yearString =>
			val split = yearString.split("-")
			split(0).replace("\"", "").toInt
		}.distinct
		val moviesInYear = years.flatMap { year => Queries.getAllMovieNamesOfYear(year.toString) }
//		val moviesInYear = List()

		val movieResource = g.getObjectOfType("dbpedia-owl:Film")
		val currentMovieNames = g.getObjectsForSubjectAndPredicate(movieResource, "dbpprop:name")
		val moviesWithSimilarName = movieNames.filter { movieWithName =>
			val l = currentMovieNames.map { movieName =>
				val l = StringUtils.getLevenshteinDistance(movieWithName.name, movieName)
//				println(f"$l, M1: #${movieWithName.name}#, M2: #$movieName#")
				l
			}.min
			l < 5
		}
		(moviesInYear ::: moviesWithSimilarName).distinct
	}

	def merge(triples: TripleGraph): List[(String, Double)] = {
		val candidates = findCandidateMovies(triples)
		var movieScores = Map[String, Double]()
		candidates.zipWithIndex.foreach { case (candidate, i) =>
			val score = calculateActorOverlap(triples, candidate.resource) //TODO moviename
			movieScores += (candidate.resource -> score)
			if (i % 100 == 0)
				println(s"$i/${candidates.size}")
		}
		val bestMovies = movieScores.toList.sortBy { case (movie, score) => -score }.take(5)
		bestMovies.foreach(println)
		bestMovies
	}

	def calculateActorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val threshhold = 5
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
		val canidateProducers = Queries.getAllProducersOfMovie(candidateUri)

		calculateOverlap(currentProducers, canidateProducers)
	}

	def calculateDirectorOverlap(g: TripleGraph, candidateUri: String): Double = {
		val currentDirectors = g.getObjectsFor("dbpprop:director", "dbpprop:name")
		val canidateDirectors = Queries.getAllDirectorsOfMovie(candidateUri)

		calculateOverlap(currentDirectors, canidateDirectors)
	}

	private def calculateOverlap(currentObjects: List[String], canidateObjects: List[ResourceWithName]): Double = {
		val threshhold = 5

		if (canidateObjects.isEmpty)
			return 0.0

		val similarObjects = currentObjects.flatMap { currentObject =>
			val bestMatch = canidateObjects.map { canidateObject =>
				StringUtils.getLevenshteinDistance(canidateObject.name, currentObject)
			}.min

			if (bestMatch < threshhold)
				List(currentObject)
			else
				List()
		}
		similarObjects.size.toDouble / currentObjects.size
	}
}
