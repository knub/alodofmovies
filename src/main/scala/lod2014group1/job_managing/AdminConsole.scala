package lod2014group1.job_managing

import lod2014group1.database.TaskDatabase
import lod2014group1.amqp.TaskDistributor
import lod2014group1.amqp.worker.WorkerTask

class AdminConsole {

	val db = new TaskDatabase

	def run(): Unit = {
		val reader = new jline.console.ConsoleReader
		var continue = true
		while (continue) {
			val readline = reader.readLine("> ")
			if (readline != null) {
				val command = readline.toLowerCase.trim.stripSuffix(";")
				continue = handleCommand(command)
			} else {
				print("See ya.")
				continue = false
			}
		}
	}

	def handleCommand(command: String): Boolean = {
		val ShowNextTasksPattern = """show next (\d+) tasks""".r
		val ShowNumberOfOpenTasksPattern = """open tasks|show number of open tasks""".r
		val PopulateDatabase = """populate database""".r
		val BulkLoadFor = """create bulk load file (.*)""".r
		val SendToQueue = """send (.*) tasks to queue""".r
		val Empty = "".r
		val Exit = """exit""".r
		command match {
			case ShowNextTasksPattern(nbr) =>
				db.getNextNTasks(nbr.toInt).foreach { case task =>
					println(s"${task.taskType}ing ${task.fileOrUrl} until ${task.dueDate}")
				}
			case ShowNumberOfOpenTasksPattern() =>
				println(s"There are ${db.getNumberOfOpenTasks} open tasks.")
			case PopulateDatabase() =>
				JobManager.populate()
			case BulkLoadFor(fileType) =>
				JobManager.createBulkLoadFile(fileType)
			case SendToQueue(nbr) =>
				val sup = new TaskDistributor()
				db.getNextNTasks(nbr.toInt).foreach { task =>
					sup.send(WorkerTask.fromDatabaseTask(task))
				}
			case Exit() =>
				println("See ya.")
				return false
			case Empty() =>
			case _ =>
				println("Command not known.")
		}
		true
	}

}
