package lod2014group1.job_managing

import org.slf4s.Logging
import org.joda.time.DateTime

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		val dbPopulator = new DatabasePopulator
		dbPopulator.populate()
//		run()
	}

	def run(): Unit = {
		var s = ""
		while (s != "STOP") {
			print("> ")
			s = scala.Console.readLine
			println(s)
		}
	}

}
