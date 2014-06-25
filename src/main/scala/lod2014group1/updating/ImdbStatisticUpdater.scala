package lod2014group1.updating

import java.io.{File, PrintWriter}
import java.net.URL
import scala.collection.JavaConversions._

import lod2014group1.crawling.Crawler
import lod2014group1.triplification._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


object ImdbStatisticUpdater {

	def watchUpcomingMovies() {
		val baseUrl = "http://www.imdb.com/movies-coming-soon"
		val path = "/home/tanja/Desktop/IMDB"

		val ids = (6 to 12).flatMap { currMonth: Int =>
			val m = "%02d" format currMonth
			val url = s"$baseUrl/2014-$m"
			val content = Crawler.downloadFile(new URL(url))

			val doc = Jsoup.parse(content)
			val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

			var ids: List[String] = List()

			movieIdTags.foreach { movieId: Element =>
				ids = movieId.attr("href").split("/")(2).substring(2) :: ids
			}
			ids
		}

		val format = DateTimeFormat.forPattern("y-MM-dd")
		val dateStr = format.print(new DateTime())

		val writer = new PrintWriter(new File(s"$path/$dateStr"))
		ids.sorted.foreach(writer.println)
		writer.close()

	}

	def watchExistingMovie() {
		val pages: Map[String, String] = Map(
			"" -> "data/IMDBMovie/tt2109248/main.html",
			"fullcredits" -> "data/IMDBMovie/tt2109248/fullcredits.html",
			"locations" -> "data/IMDBMovie/tt2109248/locations.html",
			"keywords" -> "data/IMDBMovie/tt2109248/keywords.html",
			"awards" -> "data/IMDBMovie/tt2109248/awards.html",
			"releaseinfo" -> "data/IMDBMovie/tt2109248/releaseinfo.html"
		)

		val movieBaseUrl = "http://www.imdb.com/title/tt2109248"
		val path = "/home/tanja/Desktop/IMDB"

		val triples = pages.flatMap { case (page, filePath) =>
			val url = movieBaseUrl + "/" + page

			try {
				val content = Crawler.downloadFile(new URL(url))
				new Triplifier().triplify(filePath, content)
			} catch {
				case e: Exception =>
					println(url + " throws error.")
					List()
			}
		}

		val format = DateTimeFormat.forPattern("y-MM-dd")
		val dateStr = format.print(new DateTime())

		val writer = new PrintWriter(new File(s"$path/tt2109248_$dateStr"))
		triples.foreach(writer.println)
		writer.close()
	}
}
