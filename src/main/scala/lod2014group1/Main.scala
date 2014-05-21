package lod2014group1
import lod2014group1.crawling.Crawler
import org.slf4s.Logging
import lod2014group1.apis._
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.rdf._
import org.joda.time.DateTime
import lod2014group1.rdf.RdfMovieResource._
import lod2014group1.amqp._
import lod2014group1.amqp.WorkerTask

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			Triplifier.go
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
    		val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
    		tmdb.crawl
		} else if (args contains "rabbit-worker") {
			val worker = new WorkReceiver("tasks", "answers")
			worker.listen()
		} else if (args contains "rabbit-server") {
			new Thread(new RPCServer("answers")).start();
			new Thread(new RPCServer("answers")).start();
			new Thread(new RPCServer("answers")).start();

			val crawlTask1 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Kung_Fu_Panda", "task_id" -> "1", "content" -> FileContent.longString))
			val triplifyTask1 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Kung_Fu_Panda", "task_id" -> "2", "content" -> FileContent.longString))
			val crawlTask2 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Fight_Club", "task_id" -> "3", "content" -> FileContent.longString))
			val triplifyTask2 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Fight_Club", "task_id" -> "4", "content" -> FileContent.longString))
			val crawlTask3 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://Godzilla", "task_id" -> "5", "content" -> FileContent.longString))
			val triplifyTask3 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "Godzilla", "task_id" -> "6", "content" -> FileContent.longString))
			val crawlTask4 = WorkerTask(TaskType.Crawl.toString, Map("uri" -> "http://August_Rush", "task_id" -> "7", "content" -> FileContent.longString))
			val triplifyTask4 = WorkerTask(TaskType.Triplify.toString, Map("uri" -> "August_Rush", "task_id" -> "8", "content" -> FileContent.longString))
			val dummyTask = WorkerTask(null, Map())
			val sup = new Supervisor("tasks")

			(0 to 1000).foreach { _ =>
				sup.send(crawlTask1)
				sup.send(crawlTask2)
				sup.send(crawlTask3)
				sup.send(crawlTask4)
				sup.send(triplifyTask1)
				sup.send(triplifyTask2)
				sup.send(triplifyTask3)
				sup.send(triplifyTask4)
				sup.send(dummyTask)
			}
		} else if (args contains "crawl-ofdb") {
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.crawl
		} else if (args contains "freebase") {
		  val freebase = new lod2014group1.crawling.FreebaseFilmCrawler()
		  //freebase.getAllNotImdbMovies
		  //freebase.getFreebaseFilmsWithIMDB
		  //freebase.getExampleRdf
		  //freebase.loadAllFilmId
		  freebase.crawl
		} else if (args contains "dbpedia") {
			val dbpedia = new DBpediaAPI()
			dbpedia getAllTriplesFor "http://dbpedia.org/resource/Despicable_Me"
		} else if (args contains "ofdb-coverage"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.coverage
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}

object FileContent {
	val longString = scala.io.Source.fromFile("lorem_ipsum", "utf-8").getLines().mkString
}
