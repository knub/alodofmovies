package lod2014group1.database

import lod2014group1.Config


case class ResourceWithName(resource: String, name: String)

object Queries {

	val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)
	
	def main(args: Array[String]): Unit = {
		getObjects("lod:Moviett0101527", "dbpprop:name")
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
		val queryExecution = database.buildQuery(query)

		var results: List[ResourceWithName] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			val o = rs.get("o").toString

			results ::= ResourceWithName(s, o)
		})
		results
	}

	def getObjects(subject: String, predicate: String): List[String] = {
		val query = s"$getAllPrefixe SELECT ?o WHERE { $subject $predicate ?o . }"

		val queryExecution = database.buildQuery(query)

		var results: List[String] = List()
		database.query(queryExecution, { rs =>
			val o = rs.get("o").toString

			results ::= o
		})
		results
	}

	def getAllPrefixe : String = {
		"""
		  |prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		  |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		  |prefix dbpprop: <http://dbpedia.org/property/>
		  |prefix owl: <http://www.w3.org/2002/07/owl#>
		  |prefix dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#>
		  |prefix dbpedia-owl: <http://dbpedia.org/ontology/>
		  |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
		  |prefix lod: <http://purl.org/hpi/movie#>
		  |prefix freebase: <http://rdf.freebase.com/ns/>
		  |
		""".stripMargin
	}
}
