package lod2014group1.messaging.worker

import java.sql.Date
<<<<<<< HEAD
import lod2014group1.Config
import lod2014group1.crawling.{UriToFilename, Crawler}
import lod2014group1.database.{TaskDatabase, Task, Queries}
import lod2014group1.merging.MovieMerger
=======
import lod2014group1.database.{Task, Queries}
>>>>>>> Corrected indentation.
import lod2014group1.messaging.TaskType
import lod2014group1.triplification.TriplifyDistributor
import lod2014group1.merging.MovieMerger
import org.joda.time.DateTime


class TriplimergeWorker extends Worker{

	def execute(taskId: Long, params: Map[String, String]): TaskAnswer = {
		val fileName = params("fileName")
		val content = params("content")
		val graph = params("graph")
		val flag = params("flag")

		val triplifier = new TriplifyDistributor
		val triples = triplifier.triplify(fileName, content)

	    val mergedTriples = MovieMerger.merge(triples).map { _.toRdfTripleString() }
	
	    if (mergedTriples.isEmpty && !flag.equals("no matching")) {
        val date = new Date(new DateTime().toDate.getTime)
        val task = Task(0, TaskType.Match.toString, date, 5, fileName, true, "", graph)

        val database = new TaskDatabase
        database.insert(task)
	    }
	
		val answerMap: Map[String, String] = Map("graph" -> graph)
		new TaskAnswer(taskId, answerMap, Nil, mergedTriples)
	}
}
