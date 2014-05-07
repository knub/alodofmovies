package lod2014group1.apis

import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf._

class DBpediaAPI {

	var SPARQL_ENDPOINT = "http://dbpedia.org/sparql"

	def executeQuery(queryString: String) = {
		val queryExecution = buildQuery(queryString)

		query(queryExecution, rs => {
			System.out.println(rs)
		})
	}

	def buildQuery(queryString: String): QueryExecution = {
		val query: Query = QueryFactory.create(queryString)
		QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query)
	}

	def query(query: QueryExecution, resultFunction: (QuerySolution) => Unit) {
		val resultSet = query.execSelect
		while (resultSet.hasNext()) {
			var rs = resultSet.nextSolution()
			resultFunction(rs)
		}
		query.close
	}

}
