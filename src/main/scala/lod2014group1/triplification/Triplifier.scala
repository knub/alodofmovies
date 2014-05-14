package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import lod2014group1.{Config, I}
import org.apache.commons.io.FileUtils
import lod2014group1.crawling.ImdbMovieCrawler
import scala.collection.JavaConversions._
import org.slf4s.Logging

class Triplifier {

	def triplify(f: File): List[RdfTriple] = {
		val imdbId = f.getPath().split("/")(2).replaceAll("\\D", "")

		if (f.getName == "fullcredits.html")
			new ImdbCastTriplifier().triplify(f)
		else if (f.getName == "locations.html")
			new ImdbLocationTriplifier(imdbId).triplify(f)
		else if (f.getName == "keywords.html")
			new ImdbKeywordTriplifier(imdbId).triplify(f)
		else if (f.getName == "awards.html")
			new ImdbAwardTriplifier(imdbId).triplify(f)
		else if (f.getName == "releaseinfo.html")
			new ImdbReleaseInfoTriplifier(imdbId).triplify(f)
		else
			List()
	}
}

object Triplifier extends Logging {
	def go() {
		val triplifier = new Triplifier
		val movieDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")
		log.info("Started grabbing files.");
		val movieFiles = FileUtils.listFiles(movieDir, null, true).toList.sorted.reverse
		log.info("Found " + movieFiles.size + " movies.")
		movieFiles.take(10).foreach(println)
		val triples = I.am match {
			case Config.Person.Stefan => {
				triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
			}
			case Config.Person.Tanja => {
				//triplifier.triplify(new File("data/IMDBMovie/tt0758758/locations.html")) :::
				//triplifier.triplify(new File("data/IMDBMovie/tt0054331/keywords.html"))
				//triplifier.triplify(new File("data/IMDBMovie/tt0758758/awards.html"))
				triplifier.triplify(new File("data/IMDBMovie/tt0050900/releaseinfo.html"))
			}
		}

//		println(
//			"""
//			  |@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
//			  |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
//			  |@prefix dbpprop: <http://dbpedia.org/property/> .
//			  |@prefix owl: <http://www.w3.org/2002/07/owl#> .
//			  |@prefix dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#> .
//			  |@prefix dbpedia-owl: <http://dbpedia.org/ontology/> .
//			  |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
//			  |@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
//			  |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
//			  |@prefix lod: <http://purl.org/hpi/movie#> .
//			  |
//			""".stripMargin)

		triples.foreach(println)
	}
}
