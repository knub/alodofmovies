package lod2014group1.job_managing

import scala.slick.driver.SQLiteDriver.simple._
import org.slf4s.Logging
import scala.slick.jdbc.meta.MTable
import java.sql.Date

class Task(tag: Tag) extends Table[(Int, String, String)](tag, "tasks") {
	def id         = column[Int]("task_id", O.PrimaryKey, O.AutoInc)
	def taskType   = column[String]("task_type")
	def dueDate    = column[Date]("due_date")
	def importance = column[Int]("importance")
	def params     = column[String]("params")

	def * = (id, taskType, dueDate, importance, params)
}

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		run()
	}

	def run(): Unit = {
		val database =  Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC")
		database withSession { implicit session =>
			val tasks = TableQuery[Task]
			if (MTable.getTables("tasks").list.isEmpty) {
				tasks.ddl.create
			} else {
				println("Not creating table.")
			}

			tasks.insert(0, "abc", "def")

			var s = ""
			while (s != "STOP") {
				print("> ")
				s = scala.Console.readLine
				println(s)
			}
		}

	}

}
