package lod2014group1.database

import lod2014group1.rdf.RdfTriple
import java.io.PrintWriter

class BulkLoadWriter {

	var writer: PrintWriter = _

	def newFile(fileName: String): Unit = {
		writer = new PrintWriter(s"bulk/$fileName")
		writer.println(
			"""@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
			|@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
			|@prefix dbpprop: <http://dbpedia.org/property/> .
			|@prefix owl: <http://www.w3.org/2002/07/owl#> .
			|@prefix dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#> .
			|@prefix dbpedia-owl: <http://dbpedia.org/ontology/> .
			|@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
			|@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
			|@prefix foaf: <http://xmlns.com/foaf/0.1/> .
			|@prefix lod: <http://purl.org/hpi/movie#> .
			|@prefix freebase: <http://rdf.freebase.com/ns/> .
			""".stripMargin)

	}
	def addTriples(triples: List[RdfTriple]): Unit = {
		triples.foreach { triple =>
			writer.println(triple)
		}
	}

	def bulkLoad: Unit = {
		writer.close()
	}

}
