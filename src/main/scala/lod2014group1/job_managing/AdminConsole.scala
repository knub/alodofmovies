package lod2014group1.job_managing

class AdminConsole {

	val db = new TaskDatabase

	def run(): Unit = {
		val reader = new jline.console.ConsoleReader
		var continue = true;
		while (continue) {
			val s = reader.readLine("> ").toLowerCase.trim.stripSuffix(";")
			continue = handleCommand(s)
		}
	}

	def handleCommand(command: String): Boolean = {
		val ShowNextTasksPattern = """show next (\d+) tasks""".r
		val ShowNumberOfOpenTasksPattern = """open tasks|show number of open tasks""".r
		val Exit = """exit""".r
		command match {
			case ShowNextTasksPattern(nbr) => {
				db.getNextNTasks(nbr.toInt).foreach { case task =>
					println(s"${task.taskType}ing ${task.fileOrUrl} until ${task.dueDate}")
				}
			}
			case ShowNumberOfOpenTasksPattern() => {
				println(s"There are ${db.getNumberOfOpenTasks} open tasks.")
			}
			case Exit() => {
				println("See ya.")
				return false
			};
			case _ => {
				println("Command not known.")
			}
		}
		true
	}

}
