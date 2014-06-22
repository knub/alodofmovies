package lod2014group1.database

import lod2014group1.rdf.RdfObject


case class MovieWithName(resource: String, name: String)
case class ActorsWithName(resource: String, name: String)

object Queries {

	def getAllMovieNames: List[MovieWithName] = {
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

		List()
	}

	def getAllMovieNamesOfYear(year: String): List[MovieWithName] = {
		val query =
			s"""
			  |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			  |prefix dbpprop: <http://dbpedia.org/property/>
			  |prefix dbpedia-owl: <http://dbpedia.org/ontology/>
			  |
			  |SELECT ?s ?o WHERE {
			  |  ?s rdf:type dbpedia-owl:Film .
			  |  ?s dbpprop:years $year .
			  |  ?s dbpprop:name ?o
			  |}
			""".stripMargin

		List()
	}

	def getAllActorsOfMovie(movie: String): List[ActorsWithName] = {
		val query =
			s"""
			  |prefix dbpprop: <http://dbpedia.org/property/>
			  |
			  |SELECT ?s ?o WHERE {
			  |  <$movie> dbpprop:starring ?s .
			  |  ?s dbpprop:name ?o
			  |}
			""".stripMargin

		List()
	}

}
