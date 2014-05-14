package lod2014group1.job_managing

import org.slf4s.Logging
import org.joda.time.DateTime

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		console
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
