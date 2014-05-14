package lod2014group1.job_managing

import java.io.File
import lod2014group1.Config
import lod2014group1.crawling.ImdbMovieCrawler
import org.apache.commons.io.FileUtils
import org.slf4s.Logging
import scala.collection.JavaConversions._
import java.sql.Date

class DatabasePopulator extends Logging {

	val BATCH_INSERT_SIZE = 10000

	def populate(): Unit = {
		val movieDir = new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}/")
		log.info("Started grabbing files.")
		val movieFiles = FileUtils.listFiles(movieDir, null, true).toList
		log.info("Grabbed files.")
		val db = new TaskDatabase
		movieFiles.grouped(BATCH_INSERT_SIZE).zipWithIndex.foreach { case (movieFilesBatch, i) =>
			db.insertAll(movieFilesBatch.map { f =>
				(0, "triplify", new Date(2014 - 1900, 5 - 1, 20), 5.toByte, f.getCanonicalPath.split("/data/")(1))
			}: _*)
			log.info("%8d/%d".format(i * BATCH_INSERT_SIZE, movieFiles.size))
		}
	}
}
