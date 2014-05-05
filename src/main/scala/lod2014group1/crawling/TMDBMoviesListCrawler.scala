package lod2014group1.crawling

import org.slf4s.Logging
import org.apache.http.client.utils.URIBuilder
import scalaj.http.Http
import scalaj.http.HttpOptions
import java.io.InputStreamReader
import net.liftweb.json.JsonParser
import lod2014group1.Config
import java.io.File


object TMDBMoviesListCrawler {
	val MOVIE_URL  =  "https://api.themoviedb.org/3/movie/%s"
	lazy val LATEST_ID = Http("https://api.themoviedb.org/3/movie/latest").options(HttpOptions.connTimeout(5000), 
		HttpOptions.readTimeout(5000)).param("api_key", Config.TMDB_API){inputStream => 
  		JsonParser.parse(new InputStreamReader(inputStream)) \\ "id"
}
	val BASE_DIR_NAME = "TMDBMoviesList"
}

class TMDBMoviesListCrawler extends Crawler with Logging{

	def crawl: Unit = {
		implicit val formats = net.liftweb.json.DefaultFormats
		val id = TMDBMoviesListCrawler.LATEST_ID
		println(id.extract[Int])


	}

	def determineFileName(uri: URIBuilder): File = {
		new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/test")
	}

}