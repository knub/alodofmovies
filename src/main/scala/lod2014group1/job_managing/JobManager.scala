package lod2014group1.job_managing

import scala.slick.driver.SQLiteDriver.simple._
import org.slf4s.Logging
import scala.slick.jdbc.meta.MTable

class Crawl(tag: Tag) extends Table[(Int, String, String)](tag, "crawls") {
	def id = column[Int]("CRAWL_ID", O.PrimaryKey, O.AutoInc)
	def uri = column[String]("URI")
	def file = column[String]("FILE")

	def * = (id, uri, file)
}

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		run()
	}

	def run(): Unit = {
		val database =  Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC")
		database withSession { implicit session =>
			val crawls = TableQuery[Crawl]
			if (MTable.getTables("crawls").list.isEmpty) {
				crawls.ddl.create
			} else {
				println("Not creating table.")
			}

			crawls.insert(0, "abc", "def")

			var s = ""
			while (s != "STOP") {
				print("> ")
				s = scala.Console.readLine
				println(s)
			}
		}

	}

}
