package lod2014group1.database

import lod2014group1.Config


case class ResourceWithName(resource: String, name: String)

object Queries {

	def main(args: Array[String]): Unit = {
		getAllActorsOfMovie("http://purl.org/hpi/movie#Moviett0101527")
		getAllMovieNames
		getAllMovieNamesOfYear("1991")
	}

	def getAllMovieNames: List[ResourceWithName] = {
		val query =
			"""
			  |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			  |prefix dbpprop: <http://dbpedia.org/property/>
			  |prefix dbpedia-owl: <http://dbpedia.org/ontology/>
			  |
			  |SELECT ?s ?o WHERE {
			  |  ?s rdf:type dbpedia-owl:Film .
			  |  ?s dbpprop:name ?o
			  |}
			""".stripMargin

		extractResourcesWithNameFrom(query)
	}

	def getAllMovieNamesOfYear(year: String): List[ResourceWithName] = {
		val query =
			s"""
			  |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			  |prefix dbpprop: <http://dbpedia.org/property/>
			  |prefix dbpedia-owl: <http://dbpedia.org/ontology/>
			  |
			  |SELECT ?s ?o WHERE {
			  |  ?s rdf:type dbpedia-owl:Film .
			  |  ?s dbpprop:years "$year" .
			  |  ?s dbpprop:name ?o
			  |}
			""".stripMargin

		extractResourcesWithNameFrom(query)
	}

	def getAllActorsOfMovie(movie: String): List[ResourceWithName] = {
		val query =
			s"""
			  |prefix dbpprop: <http://dbpedia.org/property/>
			  |
			  |SELECT ?s ?o WHERE {
			  |  <$movie> dbpprop:starring ?s .
			  |  ?s dbpprop:name ?o
			  |}
			""".stripMargin

		extractResourcesWithNameFrom(query)
	}


	def extractResourcesWithNameFrom(query: String): List[ResourceWithName] = {
		val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)
		val queryExecution = database.buildQuery(query)

		var results: List[ResourceWithName] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			val o = rs.get("o").toString

			results ::= ResourceWithName(s, o)
		})
		results
	}

}
