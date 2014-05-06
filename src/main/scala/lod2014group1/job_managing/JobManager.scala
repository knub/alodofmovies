package lod2014group1.job_managing

import scala.slick.driver.SQLiteDriver.simple._
import org.slf4s.Logging

object JobManager extends App with Logging {

	override def main(args: Array[String]): Unit = {
		run()
	}

	def run(): Unit = {
		Database.forURL("jdbc:sqlite:test.db", driver = "org.sqlite.JDBC") withSession { implicit session =>
			class Crawl(tag: Tag) extends Table[(Int, String, String)](tag, "CRAWL") {
				def id = column[Int]("CRAWL_ID", O.PrimaryKey)
				def uri = column[String]("URI")
				def file = column[String]("FILE")

				def * = (id, uri, file)
			}
			val crawls = TableQuery[Crawl]


			crawls.ddl.create

			crawls += (1, "abc", "def")
		}

	}

}
