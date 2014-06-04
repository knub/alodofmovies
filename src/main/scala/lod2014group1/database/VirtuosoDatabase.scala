package lod2014group1.database

import com.hp.hpl.jena.query._
import lod2014group1.rdf._
import scala.collection.mutable.Map
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource

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
	var results: Map[String, List[String]] = Map()
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

class VirtuosoRemoteDatabase(sparqlEndpoint: String) extends VirtuosoDatabase {
println("Note: Using %s as remote database.".format(sparqlEndpoint))
def buildQuery(queryString: String): QueryExecution = {
	val query: Query = QueryFactory.create(queryString.replace("FROM <graph>", ""))
		QueryExecutionFactory.sparqlService(sparqlEndpoint, query)
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
