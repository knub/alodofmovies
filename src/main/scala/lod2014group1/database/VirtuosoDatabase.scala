package lod2014group1.database

import com.hp.hpl.jena.query._
import com.hp.hpl.jena.graph._
import virtuoso.jena.driver.{VirtuosoQueryExecutionFactory, VirtGraph}
import lod2014group1.rdf._
import scala.collection.mutable.Map
import scala.sys.process._
import java.io._
import org.apache.commons.io.FileUtils
import lod2014group1.rdf.RdfString
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import com.hp.hpl.jena.update._
import lod2014group1.rdf.RdfString
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfTripleString
import lod2014group1.rdf.RdfString
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfTripleString

abstract class VirtuosoDatabase {
	def connectToDatabase
	def closeConnection

	def buildQuery(queryString: String): QueryExecution

	def query(query: QueryExecution, resultFunction: (QuerySolution) => Unit) {
		val resultSet = query.execSelect
		while (resultSet.hasNext()) {
			val rs = resultSet.nextSolution()
			resultFunction(rs)
		}
		query.close
	}

	def queryToMap(query: QueryExecution): Result = {
		queryToMap(query, List())
	}

	def queryToMap(query: QueryExecution, columnNames: List[String]): Result = {
		var results: List[Map[String, String]] = List()
		var keys: List[String] = columnNames
		var map: Map[String, String] = Map()
		val queryResult = query.execSelect
		while (queryResult.hasNext) {
			val rs = queryResult.nextSolution
			if (keys.isEmpty) {
				val it = rs.varNames
				while(it.hasNext)
					keys = it.next.toString :: keys
			}

			keys.filter(_ != "graph").foreach(key => {
				if (rs.get(key) != null)
					map.put(key, rs.get(key).toString)
				else map.put(key, "")
			})
			results = map :: results
			map = Map()
		}
		query.close()
		results
	}

	def queryKeyToMap(query: QueryExecution, queryKey: String): Map[String, List[String]] = {
		val results: Map[String, List[String]] = Map()
		var keys: List[String] = List()
		val queryResult = query.execSelect
		while (queryResult.hasNext) {
			val rs = queryResult.nextSolution
			if (keys.isEmpty) {
				var it = rs.varNames
				while(it.hasNext)
					keys = (it.next).toString :: keys
			}
			keys = keys.filter(_ != "graph")
			val valueKey = keys.filter(_ != queryKey).last
			if (keys.size != 2)
				throw new IllegalArgumentException("Works only for two columns.")

			val key = rs.get(queryKey).toString
			if (!results.contains(key))
				results(key) = List()
			results(key) = rs.get(valueKey).toString :: results(key)
		}
		query.close()
		results

	}
	def queryKeyToMap(query: QueryExecution): Map[String, List[String]] = {
		queryKeyToMap(query, "key")
	}
}


class VirtuosoLocalDatabase(sparqlEndpoint: String) extends VirtuosoRemoteDatabase(sparqlEndpoint) {
	private def isql = "isql"

	private val bulkLoadCommands = "bulk/bulk_load.isql"
	private val bulkLoadFile = "bulk/load.bulk"

	private def createTempBulkLoadFile(graph: String): File = {
		val fileContent = FileUtils.readFileToString(new File(bulkLoadCommands))
		val tmpFile = File.createTempFile("bulk", null, null)
		FileUtils.writeStringToFile(tmpFile,
			fileContent.replace("<graph>", graph))
		tmpFile
	}

	private def deleteTempBulkLoadFile(file: File): Unit = {
		FileUtils.forceDelete(file)
	}

	private def bulkLoad(graph: String): Unit = {
		val file = createTempBulkLoadFile(graph)
		println(isql.#<(new FileInputStream(file)).!!)
		deleteTempBulkLoadFile(file)
	}

	def bulkLoad(triples: List[RdfTripleString], graph: String): Unit = {
		buildRdfFile(triples)
		bulkLoad(graph)
	}

	private def buildRdfFile(triples: List[RdfTripleString]) {
		val f = new BufferedWriter(new FileWriter(bulkLoadFile))
		writeFileHeader(f)
		println(triples.size)
		triples.foreach(triple => {
			f.write(triple.toString() + "\n")
		})
		f.close()
	}

	private def writeFileHeader(f: BufferedWriter): Unit = {
		f.write(
			"""
			  |@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
			  |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
			  |@prefix dbpprop: <http://dbpedia.org/property/> .
			  |@prefix owl: <http://www.w3.org/2002/07/owl#> .
			  |@prefix dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#> .
			  |@prefix dbpedia-owl: <http://dbpedia.org/ontology/> .
			  |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
			  |@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
			  |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
			  |@prefix lod: <http://purl.org/hpi/movie#> .
			  |@prefix category: <http://dbpedia.org/resource/Category:> .
			  |@prefix freebase: <http://rdf.freebase.com/ns/> .
			""".stripMargin)
	}
}


class VirtuosoRemoteDatabase(sparqlEndpoint: String) extends VirtuosoDatabase {
//	println("Note: Using %s as remote database.".format(sparqlEndpoint))
	val g = new VirtGraph("http://172.16.22.196/imdb", "jdbc:virtuoso://172.16.22.196:1111", "dba", "dba")
	def buildQuery(queryString: String): QueryExecution = {
		val query: Query = QueryFactory.create(queryString.replace("FROM <graph>", ""))
		VirtuosoQueryExecutionFactory.create(query, g)
	}

	def deleteTriples(uri: String, property: String, obj: String): Unit = {
		val n1 = NodeFactory.createURI(uri)
		val n2 = NodeFactory.createURI(property)
		val n3 = NodeFactory.createLiteral(obj)
		g.delete(new Triple(n1, n2, n3))
	}

	def closeConnection = {}
	def connectToDatabase = {}

	private def allTriplesFor(queryExecution: QueryExecution, uri: String): List[RdfTriple] = {
		var results: List[RdfTriple] = List()

		query(queryExecution, rs => {
			val p = rs.get("p").toString
			val o = rs.get("o")
			val obj = if (o.isResource)
				RdfResource(o.toString)
			else
				RdfString(o.toString)
			val t = RdfTriple(RdfResource(uri), RdfResource(p), obj)
			results ::= t
		})
		results
	}


	def allTriplesFor(url: String): List[RdfTriple] = allTriplesFor(url, url, List(url))
	def allTriplesFor(searchForUrl: String, saveAsUrl: String, visitedUris: List[String]): List[RdfTriple] = {
		val queryExecution = buildQuery("SELECT * FROM <graph> WHERE {" + searchForUrl + " ?p ?o}")
		val results: List[RdfTriple] = allTriplesFor(queryExecution, saveAsUrl)

		results
	}
}
