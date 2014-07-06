package lod2014group1.database

import com.hp.hpl.jena.graph.{NodeFactory, Triple, Node}
import com.hp.hpl.jena.shared.DeleteDeniedException
import com.hp.hpl.jena.util.iterator.ExtendedIterator
import lod2014group1.Config
import org.joda.time.DateTime
import virtuoso.jena.driver.VirtGraph
import scala.collection.JavaConversions._


case class ResourceWithName(var resource: String, var name: String)
case class ResourceWithNameAndOriginalTitle(resource: String, originalTitle: String, name: String)

object Queries {

	val database = new VirtuosoRemoteDatabase(Config.SPARQL_ENDPOINT)

	def main(args: Array[String]): Unit = {
		deleteTriplesForMovie(s"${Config.LOD_PREFIX}Moviett0365907", "http://172.16.22.196/imdb-updating")
	}

	def getAllMovieNames: List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMovieOriginalNames: List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:originalTitle ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMoviesWithNameAndOriginalTitles: List[ResourceWithNameAndOriginalTitle] = {
		val query = s"""
				$getAllPrefixe
				SELECT * FROM <${Config.IMDB_GRAPH}> WHERE {
					?s rdf:type dbpedia-owl:Film .
					?s dbpprop:name ?name .
					OPTIONAL { ?s dbpprop:originalTitle ?original } .
				}
		"""

		val queryExecution = database.buildQuery(query)
		var results: List[ResourceWithNameAndOriginalTitle] = List()
		database.query(queryExecution, { rs =>
			val s = rs.get("s").toString
			val name = rs.get("name").toString
			var original = if (rs.varNames().toList.contains("original"))
				rs.get("original").toString
			else
				null
			if (original != null) {
				original = original.substring(1, original.size - 1)
			}
			results ::= ResourceWithNameAndOriginalTitle(s, original, name)
		})
		results
	}

	def getAllMovieNamesOfYear(year: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type dbpedia-owl:Film . ?s dbpprop:years '$year' . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllMovieNamesAroundYearWithRuntime(year: String, runtime: String): List[ResourceWithName] = {
		val query = s"""
				$getAllPrefixe
				SELECT * FROM <${Config.IMDB_GRAPH}> WHERE {
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
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { <$movie> dbpprop:starring ?s . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllProducersOfMovie(movie: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { { <$movie> dbpprop:coProducer ?s } UNION { <$movie> dbpprop:producer ?s } . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}

	def getAllDirectorsOfMovie(movie: String): List[ResourceWithName] = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { <$movie> dbpprop:director ?s . ?s dbpprop:name ?o }"
		extractResourcesWithNameFrom(query)
	}
	def getAllWritersOfMovie(movie: String): List[ResourceWithName] = {
		val query =
			s"""
			   |$getAllPrefixe
			   |SELECT * FROM <${Config.IMDB_GRAPH}> WHERE {
			   |   { <$movie> dbpprop:screenplay ?s }
			   |   UNION
			   |   { <$movie> dbpprop:author ?s }
			   |   UNION
			   |   { <$movie> dbpprop:writer ?s }
			   |   UNION
			   |   { <$movie> dbpprop:story ?s } .
			   |   ?s dbpprop:name ?o
			   |}
			 """.stripMargin
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
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type lod:ReleaseInfo . <$movieResource> dbpprop:released ?s . }"
		existsResource(query)
	}

	def existsAka(movieResource: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type lod:Aka . <$movieResource> dbpprop:alternativeNames ?s . }"
		existsResource(query)
	}

	def existsAward(movieResource: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type dbpedia-owl:Award . <$movieResource> lod:hasAward ?s . }"
		existsResource(query)
	}

	def existsPerson(movieResource: String, predicate: String): Boolean = {
		val query = s"$getAllPrefixe SELECT * FROM <${Config.IMDB_GRAPH}> WHERE { ?s rdf:type dbpedia-owl:Person . <$movieResource> $predicate ?s . }"
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

	def deleteTriplesForMovie(movieResource: String, graphName: String) {
		val graph = new VirtGraph(graphName, "jdbc:virtuoso://172.16.22.196:1111", "dba", "dba")

		// get all triples of the movie resource
		val iterator: ExtendedIterator[com.hp.hpl.jena.graph.Triple] = graph.find(NodeFactory.createURI(movieResource), Node.ANY, Node.ANY)
		var triplesToDelete: List[com.hp.hpl.jena.graph.Triple] = iterator.toList.toList

		// get all resources (award, aka, releaseInfo, character) of the movie
		val otherResources = triplesToDelete.filter{ triple: com.hp.hpl.jena.graph.Triple =>
			triple.getObject.toString.startsWith(movieResource)
		}.toList

		// get all triples of other resources
		triplesToDelete = triplesToDelete ::: otherResources.flatMap{ res =>
			graph.find(NodeFactory.createURI(res.getObject.toString), Node.ANY, Node.ANY).toList
		}

		// get all triples which point to the movie or one of the other resources
		triplesToDelete = triplesToDelete ::: otherResources.flatMap{ res =>
			graph.find(Node.ANY, Node.ANY, NodeFactory.createURI(res.getObject.toString)).toList
		}

		// delete all triples
		triplesToDelete.foreach { triple =>
			try {
				graph.delete(triple)
			} catch {
				case e: DeleteDeniedException =>
					println(s"Could not delete trpile ${triple.toString}")
			}
		}
	}

	private def getAllPrefixe : String = {
		s"""
		  |prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		  |prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		  |prefix dbpprop: <http://dbpedia.org/property/>
		  |prefix owl: <http://www.w3.org/2002/07/owl#>
		  |prefix dcterms: <http://dublincore.org/2010/10/11/dcterms.rdf#>
		  |prefix dbpedia-owl: <http://dbpedia.org/ontology/>
		  |prefix xsd: <http://www.w3.org/2001/XMLSchema#>
		  |prefix lod: <${Config.LOD_PREFIX}>
		  |prefix freebase: <http://rdf.freebase.com/ns/>
		  |
		""".stripMargin
	}

	def deleteNameAndOriginalTitleTriples(resource: ResourceWithNameAndOriginalTitle): Unit = {
		database.deleteTriples(resource.resource, "http://dbpedia.org/property/originalTitle", resource.originalTitle)
		database.deleteTriples(resource.resource, "http://dbpedia.org/property/name", resource.name)
	}
}
