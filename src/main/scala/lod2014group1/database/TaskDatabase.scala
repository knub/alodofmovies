package lod2014group1.database

import java.sql.Date
import org.joda.time.DateTime
import org.slf4s.Logging
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

case class Task(id: Long, taskType: String, dueDate: Date, importance: Byte, fileOrUrl: String, finished: Boolean, flag: String, graph: String)

class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
	def id         = column[Long]("task_id", O.PrimaryKey, O.AutoInc, O.DBType("BIGINT"))
	def taskType   = column[String]("task_type")
	def dueDate    = column[Date]("due_date")
	def importance = column[Byte]("importance")
	def fileOrUrl  = column[String]("file")
	def finished   = column[Boolean]("finished")
	def flag       = column[String]("flag")
	def graph      = column[String]("graph")


	def uniqueConstraint = index("unique_constraint", (taskType, fileOrUrl))

	def * = (id, taskType, dueDate, importance, fileOrUrl, finished, flag, graph) <>  (Task.tupled, Task.unapply)
}
class TaskDatabase extends Logging {
	val DATABASE_NAME = "lod"
	val database =  Database.forURL(s"jdbc:mysql://172.16.22.196:3306/$DATABASE_NAME",
		driver = "com.mysql.jdbc.Driver",
		user = "root",
		password = "dba")

	val tasks = TableQuery[TaskTable]
	createTablesIfNotExist()

	private def createTablesIfNotExist(): Unit = {
		database withSession { implicit session =>
			val tables = List(tasks)
			tables.foreach { table =>
				val tableName = table.baseTableRow.tableName
				if (MTable.getTables(tableName).list.isEmpty) {
					log.info(s"Creating table $tableName")
					table.ddl.create
				}
			}
		}
	}

	def runInDatabase[A](proc: TableQuery[TaskTable] => Session => A): A = {
		database withSession { session =>
			proc(tasks)(session)
		}
	}

	def getFilesMatching(pattern: String): List[Task] = {
		database withSession { implicit session =>
			tasks.filter { task =>
				task.fileOrUrl endsWith  s"$pattern.html"
			}.list
		}
	}

	def getNextNTasks(n: Int, minTaskId: Long): List[Task] = {
		database withSession { implicit session =>
			tasks.sortBy(t => (t.dueDate, t.importance, t.id)).filter(!_.finished).filter(_.id > minTaskId).take(n).list()
		}
	}

	def getNextNTasks(n: Int): List[Task] = {
		getNextNTasks(n, 0L)
	}

	def getNumberOfOpenTasks: Int = {
		database withSession { implicit session =>
			tasks.length.run
		}
	}

	def hasTasks(imdbId: String): Boolean = {
		database withSession { implicit session =>
			tasks.filter { t => t.fileOrUrl like s"%$imdbId%" }.exists.run
		}
	}

	def resetTasks(imdbId: String) {
		database withSession { implicit session =>
			tasks.filter { t => t.fileOrUrl === s"IMDBMovie/$imdbId/main.html" }.map(_.finished).update(false)
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

	def insertAll(values: List[Task]): Unit = {
		insertAll(values: _*)
	}

	implicit private def jodaDateToSqlDate(dt: DateTime): Date = {
		new Date(dt.toDate.getTime)
	}
}
