package lod2014group1.database

import java.io.File
import java.sql.Date
import lod2014group1.Config
import lod2014group1.crawling.ImdbMovieCrawler
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.slf4s.Logging
import scala.collection.JavaConversions._
import lod2014group1.messaging.TaskType

class DatabasePopulator extends Logging {

	val BATCH_INSERT_SIZE = 10000

	def populate(): Unit = {
		val movieDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")
		log.info("Started grabbing files.")
		val movieFiles = FileUtils.listFiles(movieDir, null, true).toList
		log.info("Grabbed files.")
//		val db = new TaskDatabase
//		movieFiles.grouped(BATCH_INSERT_SIZE).zipWithIndex.foreach { case (movieFilesBatch, i) =>
//			db.insertAll(movieFilesBatch.map { f =>
//				// CHECK GRAPH BEFORE COMMENTING IN!!!
//				Task(0, TaskType.Triplify.toString, date(2014, 6, 30), 5.toByte, f.getCanonicalPath.split("/data/")(1), false, "", Config.IMDB_GRAPH)
//			}: _*)
//			log.info("%8d/%d".format(i * BATCH_INSERT_SIZE, movieFiles.size))
//		}
	}

	private def date(year: Int, month: Int, day: Int): Date = {
		new Date(new DateTime(year, month, day, 0, 0).toDate.getTime)

	}
}
