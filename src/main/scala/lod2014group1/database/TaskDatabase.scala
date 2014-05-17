package lod2014group1.database

import java.sql.Date
import org.joda.time.DateTime
import org.slf4s.Logging
import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable

case class Task(id: Int, taskType: String, dueDate: Date, importance: Byte, fileOrUrl: String)

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
	def id         = column[Int]("task_id", O.PrimaryKey, O.AutoInc)
	def taskType   = column[String]("task_type")
	def dueDate    = column[Date]("due_date")
	def importance = column[Byte]("importance")
	def fileOrUrl  = column[String]("file")

	def uniqueConstraint = index("unique_constraint", (taskType, fileOrUrl), unique = true)

	def * = (id, taskType, dueDate, importance, fileOrUrl) <>  (Task.tupled, Task.unapply)
}
class TaskDatabase extends Logging {
	val DATABASE_NAME = "lod.db"
	val database =  Database.forURL(s"jdbc:sqlite:${DATABASE_NAME}", driver = "org.sqlite.JDBC")

	val tasks = TableQuery[TaskTable]
	createTablesIfNotExist

	private def createTablesIfNotExist(): Unit = {
		database withSession { implicit session =>
			val tables = List(tasks)
			tables.foreach { table =>
				val tableName = table.baseTableRow.tableName
				if (MTable.getTables(tableName).list.isEmpty) {
					log.info(s"Creating table $tableName")
					table.ddl.create
				} else {
					log.info(s"Table $tableName already exists.")
				}
			}
		}
	}

	def getFilesMatching(pattern: String): List[Task] = {
		database withSession { implicit session =>
			tasks.filter { task =>
				task.fileOrUrl endsWith  s"$pattern.html"
			}.list
		}
	}

	def getNextNTasks(n: Int): List[Task] = {
		database withSession { implicit session =>
			tasks.sortBy(t => (t.dueDate, t.importance)).take(n).list()
		}
	}

	def getNumberOfOpenTasks: Int = {
		database withSession { implicit session =>
			tasks.length.run
		}
	}


	def insert(task: Task): Unit = {
		database withSession { implicit session =>
			// task_id is ignored because it is an auto increment column
			tasks.insert(task)
//			(tasks returning tasks.map(._id)) += Task(0, taskType, dueDate, importance, params)
		}

	}
	def insertAll(values: Task*): Unit = {
		database withSession { implicit session =>
			tasks.insertAll(values: _*)
		}
	}

	implicit private def jodaDateToSqlDate(dt: DateTime): Date = {
		new Date(dt.toDate.getTime)
	}
}
