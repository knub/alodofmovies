package lod2014group1.database

import com.hp.hpl.jena.update.{UpdateExecutionFactory, UpdateFactory}
import lod2014group1.Config
import lod2014group1.merging.Merger
import org.joda.time.DateTime


case class ResourceWithName(var resource: String, var name: String)

object Queries {

	val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)

	def main(args: Array[String]): Unit = {
		println(getAllMovieNames.size)
	}

	def getAllMovieNames: List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMovieOriginalNames: List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:originalTitle ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMoviesWithOriginalTitles: List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:originalTitle ?o}"
		extractResourcesWithNameFrom(query)

	}

	def getAllMovieNamesOfYear(year: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:years '$year' . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMovieNamesAroundYearWithRuntime(year: String, runtime: String): List[ResourceWithName] = {
		val query = s"""
				$getAllPrefixe
				SELECT * WHERE {
					?s rdf:type dbpedia-owl:Film .
					?s dbpprop:years '$year' .
					?s dbpprop:name ?o .
		      ?s dbpprop:runtime ?r .
				  FILTER(?r => ${addToRdfInteger(runtime, -5)}
						&& ?r <= ${addToRdfInteger(runtime, 5)}
				}
		"""
 		extractResourcesWithNameFrom(query)
	}

	def addToRdfInteger(number: String, x: Int): Int = {
		number.split("\"")(1).toInt - x
	}

	def getAllActorsOfMovie(movie: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { <$movie> dbpprop:starring ?s . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllProducersOfMovie(movie: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { <$movie> dbpprop:coProducer ?s . <$movie> dbpprop:producer ?s . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllDirectorsOfMovie(movie: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * WHERE { <$movie> dbpprop:director ?s . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	private def extractResourcesWithNameFrom(query: String): List[ResourceWithName] = {
		val queryExecution = database.buildQuery(query)

		var results: List[ResourceWithName] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			val o = rs.get("o").toString
			results ::= ResourceWithName(s, o)
		})
		results
	}

	def existsReleaseInfo(movieResource: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type lod:ReleaseInfo . <$movieResource> dbpprop:released ?s . }"
		existsResource(query)
	}

	def existsAka(movieResource: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type lod:Aka . <$movieResource> dbpprop:alternativeNames ?s . }"
		existsResource(query)
	}

	def existsAward(movieResource: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Award . <$movieResource> lod:hasAward ?s . }"
		existsResource(query)
	}

	def existsPerson(movieResource: String, predicate: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * WHERE { ?s rdf:type dbpedia-owl:Person . <$movieResource> $predicate ?s . }"
		existsResource(query)
	}

	private def existsResource(query: String): Boolean = {
		val queryExecution = database.buildQuery(query)

		var results: List[String] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			results ::= s
		})
		results.size == 0
	}

	def existsTriple(subject: String, predicate: String): Boolean = {
		val query = s"$getAllPrefixe SELECT ?o WHERE { <$subject> $predicate ?o . }"
		val queryExecution = database.buildQuery(query)

		var results: List[String] = List()
		database.query(queryExecution, { rs =>
			val o = rs.get("o").toString
			results ::= o
		})
		results.size == 0
	}

	def getMovieIdsInTimeRange(fromDate: DateTime, toDate: DateTime): List[String] = {
		val fromYear = fromDate.getYear - 1
		val toYear = toDate.getYear + 1

		val query =	s"""
			   $getAllPrefixe
				SELECT ?s ?year WHERE {
					?s rdf:type dbpedia-owl:Film .
					?s dbpprop:years ?yearString .
						BIND (xsd:integer(?yearString) AS ?year) .
					FILTER (?year > $fromYear) .
					FILTER (?year < $toYear) .
				}
			"""
		val queryExecution = database.buildQuery(query)

		var results: List[String] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			results ::= s.takeRight(9)
		})
		results
	}

	def deleteTriplesForMovie(movieId: String, graph: String) {
		val query =	s"""
			   $getAllPrefixe
				DELETE FROM GRAPH <$graph> { ?resource ?p ?o }
				WHERE
				{
				  {
					?resource ?p ?o
					{
					  SELECT ?resource WHERE
					  {
						lod:Movie$movieId ?p ?resource .
						FILTER (strStarts(str(?resource), "http://purl.org/hpi/movie#"))
					  }
					}
				  }
				  UNION
				  {
					BIND (lod:Movie$movieId as ?resource) ?resource ?p ?o .
				  }
				};
			"""

		println(query)

		//val update = UpdateFactory.create(query)
		//val uExec =	UpdateExecutionFactory.createRemote(update, Config.SPARQL_ENDPOINT)
		//uExec.execute()
	}

	private def getAllPrefixe : String = {
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

	def deleteNameAndOriginalTitleTriples(graph: String, movieResource: String) {
		val query = s"""
			$getAllPrefixe
			DELETE { ?s ?p ?o }
			WHERE {
        {
          BIND (<$movieResource> as ?s)
          BIND (dbpprop:name as ?p)
          ?s ?p ?o .
        }
        UNION
        {
          BIND (<$movieResource> as ?s)
          BIND (dbpprop:originalTitle as ?p)
          ?s ?p ?o .
        }
			}
		"""
		val update = UpdateFactory.create(query)
		val uExec =	UpdateExecutionFactory.createRemote(update, Config.SPARQL_ENDPOINT + "/statements")
		uExec.execute()
	}

}
