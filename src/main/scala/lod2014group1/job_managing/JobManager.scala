package lod2014group1.job_managing

import org.slf4s.Logging
import lod2014group1.triplification.TriplifyDistributor
import java.io.File
import lod2014group1.Config
import lod2014group1.database._

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		console()
	}

	def createBulkLoadFile(fileType: String): Unit = {
		val triplifier = new TriplifyDistributor
		val database = new TaskDatabase
		val bulkLoadWriter = new BulkLoadWriter
		bulkLoadWriter.newFile(s"$fileType.bulk")
		val files = database.getFilesMatching(fileType)

		println(s"Creating bulk load file for ${files.size} files.")
		files.zipWithIndex.foreach { case (task, index) =>
			if (index % 10000 == 0)
				println(index)
			try {
				val fileName = s"${Config.DATA_FOLDER}/${task.fileOrUrl}"
				val triples = triplifier.triplify(new File(fileName))
				bulkLoadWriter.addTriples(triples)
			} catch {
				case e: Exception => println(s"Error in file ${task.fileOrUrl}")
			}
		}
		bulkLoadWriter.finishFile

	}
	def populate(): Unit = {
		val dbPopulator = new DatabasePopulator
		dbPopulator.populate()
	}

	def console(): Unit = {
		val console = new AdminConsole
		console.run()
	}

}
