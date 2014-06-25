package lod2014group1.updating

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import lod2014group1.crawling.Crawler
import java.net.URL


class ImdbComingSoonMovieUpdater() extends ImdbUpdater {

	def updateComingSoonMovies(): Unit = {
		val contents = crawlComingSoonPage()

		val ids = contents.flatMap{ content =>
			getNewMovieIds(content)
		}

		createCrawlifyTasks(ids, "")
	}

	private def getNewMovieIds(content: String): List[String] = {
		val doc = Jsoup.parse(content)
		val movieIdTags = doc.select("h4[itemprop=name] a[itemprop=url]")

		var ids: List[String] = List()
		movieIdTags.foreach { movieId: Element =>
			ids = movieId.attr("href").split("/")(2).substring(2) :: ids
		}
		ids
	}

	private def crawlComingSoonPage(): List[String] = {
		var dates: List[String] = List()
		var currentDate = new DateTime()

		(1 to 12).foreach { i =>
			val year = currentDate.getYear
			val month = currentDate.getMonthOfYear
			val dateStr = "%s-%02d".format(year, month)
			dates = dateStr :: dates
			currentDate = currentDate.plusMonths(1)
		}

		dates.flatMap { date =>
			val url = s"${ImdbUpdater.COMING_SOON_BASE_URL}/$date"
			List(Crawler.downloadFile(new URL(url)))
		}
	}

}
