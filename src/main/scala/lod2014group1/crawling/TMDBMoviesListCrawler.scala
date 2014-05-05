package lod2014group1.crawling

import org.slf4s.Logging
import org.apache.http.client.utils.URIBuilder
import scalaj.http.Http
import scalaj.http.HttpOptions
import java.io.{FileInputStream, InputStreamReader, File}
import net.liftweb.json.JsonParser
import lod2014group1.Config
import org.apache.commons.io.{IOUtils}

case class TmdbJsonResponse(id: Long, imdb_id: String)

object TMDBMoviesListCrawler {
	implicit val formats = net.liftweb.json.DefaultFormats
	val TMDB_API_KEY = IOUtils.toString(new FileInputStream(Config.TMDB_API_KEY))
	val MOVIE_URL  =  "api.themoviedb.org/3/movie/%s"
	lazy val LATEST_ID = Http("https://api.themoviedb.org/3/movie/latest").options(
		HttpOptions.connTimeout(5000),
		HttpOptions.readTimeout(5000)).param("api_key", TMDB_API_KEY) { inputStream =>
			JsonParser.parse(new InputStreamReader(inputStream))
		}.extract[TmdbJsonResponse]


	val RANDOM_ID = Http("https://api.themoviedb.org/3/movie/1").options(
		HttpOptions.connTimeout(5000),
		HttpOptions.readTimeout(5000)).param("api_key", TMDB_API_KEY).responseCode

	val BASE_DIR_NAME = "TMDBMoviesList"


}

class TMDBMoviesListCrawler extends Crawler with Logging{

	def crawl: Unit = {
		println(TMDBMoviesListCrawler.LATEST_ID)
		println(TMDBMoviesListCrawler.RANDOM_ID)


	}

	def determineFileName(uri: URIBuilder): File = {
		val urlSplit = uri.getPath.split('/')
		val queryType  = urlSplit(2)
		val id  = urlSplit(3)


		new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/$queryType/$id")
	}

}