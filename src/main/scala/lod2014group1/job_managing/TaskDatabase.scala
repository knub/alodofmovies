package lod2014group1.job_managing

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import java.sql.Date
import org.slf4s.Logging
import org.joda.time.DateTime

class Task(tag: Tag) extends Table[(Int, String, Date, Byte, String)](tag, "tasks") {
	def id         = column[Int]("task_id", O.PrimaryKey, O.AutoInc)
	def taskType   = column[String]("task_type")
	def dueDate    = column[Date]("due_date")
	def importance = column[Byte]("importance")
	def fileOrUrl  = column[String]("file")

	def uniqueConstraint = index("unique_constraint", (taskType, fileOrUrl), unique = true)

	def * = (id, taskType, dueDate, importance, fileOrUrl)
}
class TaskDatabase extends Logging {
	val DATABASE_NAME = "lod.db"
	val database =  Database.forURL(s"jdbc:sqlite:${DATABASE_NAME}", driver = "org.sqlite.JDBC")

	val tasks = TableQuery[Task]
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

	def getNextNTasks(n: Int): Query[Task, (Int, String, Date, Byte, String)] = {
		tasks.sortBy(t => (t.dueDate, t.importance)).take(n)
	}


	def insert(taskType: String, dueDate: DateTime, importance: Byte, fileOrUrl: String): Unit = {
		database withSession { implicit session =>
			// task_id is ignored because it is an auto increment column
			tasks.insert(0, taskType, dueDate, importance, fileOrUrl)
//			(tasks returning tasks.map(._id)) += Task(0, taskType, dueDate, importance, params)
		}

	}
	def insertAll(values: (Int, String, Date, Byte, String)*): Unit = {
		database withSession { implicit session =>
			tasks.insertAll(values: _*)
		}
	}

	implicit private def jodaDateToSqlDate(dt: DateTime): Date = {
		new Date(dt.toDate.getTime)
	}
}
