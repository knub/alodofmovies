package lod2014group1.merging

import java.sql.Date

import lod2014group1.messaging.TaskType
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.UriBuilder
import lod2014group1.database.{TaskDatabase, Task, Queries}
import org.apache.commons.lang3.StringUtils
import lod2014group1.triplification.FreebaseFilmsTriplifier
import lod2014group1.Config
import java.io.File

import org.joda.time.DateTime

object MovieMerger extends App{
	
	def merge(triples: List[RdfTriple]): List[RdfTriple] = {
		val tripleGraph = new TripleGraph(triples)
		val imdbId = tripleGraph.getImdbId
		
		if (imdbId == null) return List()
		
		val movieResource = tripleGraph.getObjectOfType("dbpedia-owl:Film")
		val movieTriple = tripleGraph.getTriplesForSubject(movieResource)
		val imdbResource = UriBuilder.getMovieUriFromImdbIdWithoutPrefix(imdbId)
		
		
		val releaseInfoTriple = getTriple(tripleGraph, imdbResource, "lod:ReleaseInfo", Merger.mergeReleaseInfoTriple(_:String, _:List[RdfTriple]))
		val akaTriple = getTriple(tripleGraph, imdbResource, "lod:Aka", Merger.mergeAkaTriple(_:String, _:List[RdfTriple]))
		val awardTriple = getTriple(tripleGraph, imdbResource, "dbpedia-owl:Award", Merger.mergeAkaTriple(_:String, _:List[RdfTriple]))
		
		val movieTripleToLoad = Merger.mergeMovieTriple(imdbResource, movieTriple) ::: releaseInfoTriple ::: akaTriple //::: awardTriple
		
		val personResources = tripleGraph.getObjectListOfType("dbpedia-owl:Person").distinct
		val imdbPersons = Queries.getAllActorsOfMovie(imdbResource)
		
		movieTripleToLoad ::: personResources.flatMap{personResource => 
			val personName = tripleGraph.getObjectsForSubjectAndPredicate(personResource, "dbpprop:name").head
			val personTriples = tripleGraph.getTriplesForSubject(personResource)
			val personMovieTriple = tripleGraph.getTriplesForSubjectAndObject(movieResource, personResource)
			imdbPersons.flatMap{imdbPerson =>
				if (areActorNamesEqual(imdbPerson.name, personName)){
					Merger.mergeActorTriple(imdbPerson.resource, personTriples) ::: Merger.replaceSubjectAndObject(imdbResource, imdbPerson.resource, personMovieTriple)
					//TODO Charakter if not exist
					
				} else List()
			}
		}
	}
	
	def getTriple(tripleGraph: TripleGraph, imdbResource:String, objectType:String, method: (String, List[RdfTriple]) => List[RdfTriple]): List[RdfTriple] = {
		val resources = tripleGraph.getObjectListOfType(objectType)
		resources.flatMap{ resource =>
			method(imdbResource, tripleGraph.getTriplesForSubject(resource))
		}
	}
	
	def areActorNamesEqual(imdbActorName : String, actorName : String): Boolean = {
		imdbActorName == actorName
	}
	
	override def main(args: Array[String]): Unit = {
//		val triple = (new FreebaseFilmsTriplifier).triplify(new File(s"${Config.DATA_FOLDER}/Freebase/0bdjd"))
//		//triple.foreach(println)
//		println("==================================================")
//		merge (triple).foreach(println)

    mergeFreebase()
	}



  def mergeFreebase() {
    val freebaseDir = new File(s"${Config.DATA_FOLDER}/Freebase/film/")
    val date = new Date(new DateTime().toDate.getTime)

    val taskList = freebaseDir.listFiles.map { file: File =>
      val filePath = file.getPath
      println(filePath)
      //Task(0, TaskType.Triplimerge.toString, date, 5, filePath, false, "movie", Config.FREEBASE_GRAPH)
    }

    // add tasks to database
    //val database = new TaskDatabase
    //database.insertAll(taskList)
  }

  def mergeOfdb() {
    val ofdbDir = new File(s"${Config.DATA_FOLDER}/OFDB/Movies/")
    val date = new Date(new DateTime().toDate.getTime)

    val taskList = ofdbDir.listFiles.map { file: File =>
      val filePath = file.getPath
      println(filePath)
//      List(
//        Task(0, TaskType.Triplimerge.toString, date, 5, filePath + "/Film.html", false, "", Config.OFDB_GRAPH),
//        Task(0, TaskType.Triplimerge.toString, date, 5, filePath + "/Cast.html", false, "", Config.OFDB_GRAPH)
//      )
    }

    // add tasks to database
//    val database = new TaskDatabase
//    database.insertAll(taskList)
  }

  def mergeTmdb() {
    val tmdbDir = new File(s"${Config.DATA_FOLDER}/TMDBMoviesList/movie/")
    val date = new Date(new DateTime().toDate.getTime)

//    val taskList = tmdbDir.listFiles.map { file: File =>
//      Task(0, TaskType.Triplimerge.toString, date, 5, file.getPath, false, "", Config.TMDB_GRAPH)
//    }
//
//    // add tasks to database
//    val database = new TaskDatabase
//    database.insertAll(taskList)
  }

}
