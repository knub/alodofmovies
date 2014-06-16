package lod2014group1.apis

import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf._
import lod2014group1.rdf._

object DBpediaAPI {
	val EN_SPARQL_ENDPOINT = "http://dbpedia.org/sparql"
	val DE_SPARQL_ENDPOINT = "http://de.dbpedia.org/sparql"
}

class DBpediaAPI {

	def buildQuery(queryString: String): List[QueryExecution] = {
		val query: Query = QueryFactory.create(queryString)
		List(QueryExecutionFactory.sparqlService(DBpediaAPI.EN_SPARQL_ENDPOINT, query),
			QueryExecutionFactory.sparqlService(DBpediaAPI.DE_SPARQL_ENDPOINT, query))
	}

	def buildQuery(queryString: String, endpoint: String): QueryExecution = {
		val query: Query = QueryFactory.create(queryString)
		QueryExecutionFactory.sparqlService(endpoint, query)
	}

	def query(query: QueryExecution, resultFunction: (QuerySolution) => Unit) {
		val resultSet = query.execSelect
		while (resultSet.hasNext()) {
			var rs = resultSet.nextSolution()
			resultFunction(rs)
		}
		query.close
	}

	def getFilmUris(queryString: String): List[String] = {
		val queryExecution = buildQuery(queryString, DBpediaAPI.EN_SPARQL_ENDPOINT)

		var films: List[String]= List()
		query(queryExecution, rs => {
			films = (rs.get("film").toString) :: films
		})

		println(films)

		films
	}


	def getAllTriplesFor(resource: String): List[RdfTriple] = {
		getAllTriplesFor(resource, DBpediaAPI.EN_SPARQL_ENDPOINT)
	}

	def getAllTriplesFor(resource: String, endpoint: String): List[RdfTriple] = {
		val queryString =
			"""
			  SELECT ?p ?o
	 		  WHERE {
	  		    <%s> ?p ?o
		 	  }
			""" format resource

		val queryExecution = buildQuery(queryString, endpoint)

		var triples: List[RdfTriple] = List()
		query(queryExecution, rs => {
			var p = rs.get("p")
			var o = rs.get("o")
			triples = RdfTriple(RdfResource(resource), RdfResource(p.toString()), RdfResource(o.toString())) :: triples
		})

		println(triples)

		triples
	}

	def getAllMoviesWithFreebase(): List[String] = {
		val queryString =
			"""
			  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
			  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			  PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>
			  PREFIX yago: <http://dbpedia.org/class/yago/>
			  PREFIX owl: <http://www.w3.org/2002/07/owl#>

			  SELECT distinct ?film
			  WHERE {
			    {
			      ?film rdf:type dbpedia-owl:Film .
			    }
			    UNION
			    {
			      ?class rdfs:subClassOf* yago:Movie106613686 .
			      ?film rdf:type ?class .
			    }
			      ?film owl:sameAs ?freebase .
			      FILTER regex(?freebase, "http://rdf.freebase.com/ns") .
			  }
			"""

		getFilmUris(queryString)
	}


	def getAllMoviesWithoutFreebase() = {
		val queryString =
		"""
		  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		  PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>
		  PREFIX yago: <http://dbpedia.org/class/yago/>
		  PREFIX owl: <http://www.w3.org/2002/07/owl#>

		  SELECT distinct ?film
		  WHERE {
		    {
		      ?film rdf:type dbpedia-owl:Film .
		    }
		    UNION
		    {
		      ?class rdfs:subClassOf* yago:Movie106613686 .
		      ?film rdf:type ?class .
		    }
		    MINUS
		    {
		      ?film owl:sameAs ?freebase .
		      FILTER regex(?freebase, "http://rdf.freebase.com/ns") .
		    }
		  }
		"""

		getFilmUris(queryString)
	}

}
