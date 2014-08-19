package lod2014group1.merging

import java.sql.Date
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.UriBuilder
import lod2014group1.database.{TaskDatabase, Task, Queries}
import lod2014group1.Config
import java.io.File
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import lod2014group1.triplification.{TriplifyDistributor, FreebaseFilmsTriplifier, TmdbMovieTriplifier}

object MovieMerger extends App{


  override def main(args: Array[String]): Unit = {
    val triplifier = new TriplifyDistributor
    val content = FileUtils.readFileToString(new File("/home/tanja/Repositories/alodofmovies/data/OFDB/Movies/145135/Film.html"))
    val triples = triplifier.triplify("OFDB/Movies/145135/Film.html", content)

    val mergedTriples = MovieMerger.merge(triples).map {
      _.toRdfTripleString()
    }
  }

	def merge(triples: List[RdfTriple]): List[RdfTriple] = {
		val tripleGraph = new TripleGraph(triples)

		val imdbId = tripleGraph.getImdbId
		if (imdbId == null) {
      // match movie
      //val matcher = new MovieMatcher()
      //matcher.merge(tripleGraph)
    }

		val movieResource = tripleGraph.getObjectOfType("dbpedia-owl:Film")
    if (movieResource == null)
      return List()

		val movieTriple = tripleGraph.getTriplesForSubject(movieResource)
		val imdbResource = UriBuilder.getMovieUriFromImdbIdWithoutPrefix(imdbId)
		
		val releaseInfoTriple = getTriple(tripleGraph, imdbResource, "lod:ReleaseInfo", Merger.mergeReleaseInfoTriple(_:String, _:List[RdfTriple]))
		val akaTriple = getTriple(tripleGraph, imdbResource, "lod:Aka", Merger.mergeAkaTriple(_:String, _:List[RdfTriple]))
    // merge award
    //	val awardTriple = getTriple(tripleGraph, imdbResource, "dbpedia-owl:Awad", Merger.mergeAkaTriple(_:String, _:List[RdfTriple]))
		
		val movieTriplesToLoad = Merger.mergeMovieTriple(imdbResource, movieTriple) ::: releaseInfoTriple ::: akaTriple

		val personResources = tripleGraph.getObjectListOfType("dbpedia-owl:Person").distinct
		val imdbPersons = Queries.getAllActorsOfMovie(imdbResource)
		
		movieTriplesToLoad ::: personResources.flatMap { personResource =>
			val personName = tripleGraph.getObjectsForSubjectAndPredicate(personResource, "dbpprop:name").head
			val personTriples = tripleGraph.getTriplesForSubject(personResource)
			val personMovieTriple = tripleGraph.getTriplesForSubjectAndObject(movieResource, personResource)

			imdbPersons.flatMap { imdbPerson =>
				if (areActorNamesEqual(imdbPerson.name, personName)) {
					Merger.mergeActorTriple(imdbPerson.resource, personTriples) ::: Merger.replaceSubjectAndObject(imdbResource, imdbPerson.resource, personMovieTriple)
					// merge charakter
				} else
          List()
			}
		}

    movieTriplesToLoad
	}
	
	private def getTriple(tripleGraph: TripleGraph, imdbResource:String, objectType:String, method: (String, List[RdfTriple]) => List[RdfTriple]): List[RdfTriple] = {
		val resources = tripleGraph.getObjectListOfType(objectType)
		resources.flatMap{ resource =>
			method(imdbResource, tripleGraph.getTriplesForSubject(resource))
		}
	}
	
	private def areActorNamesEqual(imdbActorName : String, actorName : String): Boolean = {
		imdbActorName == actorName
	}

}
