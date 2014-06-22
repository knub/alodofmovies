package lod2014group1.database

import com.hp.hpl.jena.query.QueryExecution
import lod2014group1.Config
import lod2014group1.rdf.{RdfString, RdfResource, RdfTriple, RdfObject}


case class MovieWithName(resource: String, name: String)
case class ActorsWithName(resource: String, name: String)

object Queries {

	def main(args: Array[String]): Unit = {
		getAllMovieNames().foreach(println)
	}

	def getAllMovieNames(): List[MovieWithName] = {
		val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)

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

		val queryExecution = database.buildQuery(query)

		var results: List[MovieWithName] = List()

		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			val o = rs.get("o").toString

			results ::= new MovieWithName(s, o)
		})

		results
	}

	def getAllMovieNamesOfYear(year: String): List[MovieWithName] = {
		val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)

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
		val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)

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
