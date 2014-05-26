package lod2014group1.job_managing

import org.slf4s.Logging
import lod2014group1.triplification.Triplifier
import java.io.File
import lod2014group1.Config
import lod2014group1.database._

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		console
//		val triplifier = new Triplifier
//		val database = new TaskDatabase
//		val bulkLoadWriter = new BulkLoadWriter
//		bulkLoadWriter.newFile("keywords.bulk")
//		database.getFilesMatching("keywords").foreach { task =>
//			val triples = triplifier.triplify(new File(s"${Config.DATA_FOLDER}/${task.fileOrUrl}"))
//			bulkLoadWriter.addTriples(triples)
//		}
//		bulkLoadWriter.bulkLoad
//		println(database.getFilesMatching("fullcredits").size)
	}

	def populate(): Unit = {
		val dbPopulator = new DatabasePopulator
		dbPopulator.populate()
	}

	def console(): Unit = {
		val console = new AdminConsole
		console.run
	}

}
