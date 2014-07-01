package lod2014group1

import lod2014group1.crawling.Crawler
import lod2014group1.database.{TaskDatabase, Queries}
import org.slf4s.Logging
import lod2014group1.triplification.{TmdbMovieTriplifier, TriplifyDistributor}
import lod2014group1.messaging._
import lod2014group1.merging.MovieMatcher
import lod2014group1.updating.{UpdateScheduler, ImdbStatisticUpdater}
import lod2014group1.job_managing.OfflineTaskRunner
import java.io.File
import lod2014group1.triplification.FreebaseFilmsTriplifier

object Main extends App with Logging {

	override def main(args: Array[String]): Unit = {
		log.debug("Started.")
		log.info("Arguments: " + args.toList)
		if (args contains "triplify") {
			TriplifyDistributor.go()
		} else if (args contains "crawl-imdb") {
			Crawler.crawl
		} else if (args contains "crawl-tmdb") {
      val tmdb = new lod2014group1.crawling.TMDBMoviesListCrawler()
      tmdb.crawl
		} else if (args contains "rabbit-worker") {
			val worker = new WorkReceiver("tasks", "answers")
			worker.init()
			worker.listen()
		} else if (args contains "rabbit-server") {
			new Thread(new AmqpMessageListenerThread("answers")).run()
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
		} else if (args contains "ofdb-clean"){
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.clean
		} else if (args contains "ofdb-coverage") {
			val ofdb = new lod2014group1.crawling.OFDBMovieCrawler()
			ofdb.coverage
		} else if (args contains "offline-task-runner") {
			val taskRunner = new OfflineTaskRunner()
			taskRunner.runTasks(800000)
		} else if (args contains "watch-imdb") {
			ImdbStatisticUpdater.watchUpcomingMovies()
			ImdbStatisticUpdater.watchExistingMovie()
		} else if (args contains "merge-tmdb") {
			val merger = new MovieMatcher()

			I.am match {
				case Config.Person.Rice =>
					val dir = new File (s"${Config.DATA_FOLDER}/Freebase/film/")
					merger.runStatistic(dir, new FreebaseFilmsTriplifier())
					return
				case default => 
					val tmdbDir = new File(s"${Config.DATA_FOLDER}/TMDBMoviesList/movie")
					merger.runStatistic(tmdbDir, new TmdbMovieTriplifier())
			}
		} else if (args contains "update-daily") {
			new UpdateScheduler().update()
		} else if (args contains "fixx") {
			val taskDb = new TaskDatabase
			val oldResources = Queries.getAllMoviesWithOriginalTitles
			oldResources.take(1).map(_.resource).zipWithIndex.foreach { case (resource, index) =>
				Queries.deleteNameAndOriginalTitleTriples("http://172.16.22.196/imdb", resource)
				taskDb.resetTasks(resource.split("movie#Movie")(1))
				if (index % 5 == 0) println(index)
			}
		} else {
			log.warn("Please pass a parameter to indicate what you want to do, e.g. run `gradle crawl` or `gradle triplify`.")
		}
		log.debug("Finished.")
	}
}

object FileContent {
	val longString = scala.io.Source.fromFile("lorem_ipsum", "utf-8").getLines().mkString
}
