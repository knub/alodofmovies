package lod2014group1.job_managing

import org.slf4s.Logging
import org.joda.time.DateTime

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		val database = new TaskDatabase()
		println(database.getFilesMatching("keywords").size)
		println(database.getFilesMatching("fullcredits").size)
//		database.getFilesMatching("keywords").foreach { task =>
//			System.out.println(task.fileOrUrl);
//		}
//		console
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
