package lod2014group1.crawling

import org.slf4s.Logging
import org.apache.http.client.utils.URIBuilder
import scalaj.http.Http
import scalaj.http.HttpOptions
import java.io._
import net.liftweb.json.JsonParser
import lod2014group1.Config
import org.apache.commons.io.{FileUtils, IOUtils}
import java.net.URL

case class TmdbJsonResponse(id: Long, imdb_id: String)

object TMDBMoviesListCrawler {
	val BASE_DIR_NAME = "TMDBMoviesList"


	val TMDB_API_KEY = IOUtils.toString(new FileInputStream(Config.TMDB_API_KEY))
	val MOVIE_URL  =  "https://api.themoviedb.org/3/movie/%s"
	val ADDITIONAL_INFO = "credits,keywords,images,videos,alternative_titles,releases,similar"

	implicit val formats = net.liftweb.json.DefaultFormats
	lazy val LATEST_ID = movieRequest(MOVIE_URL.format("latest")) { inputStream => JsonParser.parse(
				new InputStreamReader(inputStream))}.extract[TmdbJsonResponse]

	def movieRequest(url: String): Http.Request = {
		Http(url).options(HttpOptions.connTimeout(5000),
			HttpOptions.readTimeout(5000)).params("api_key" -> TMDB_API_KEY, "append_to_response" -> ADDITIONAL_INFO)
	}

}

class TMDBMoviesListCrawler extends Crawler with Logging{

	def crawl: Unit = {
		val latest_id = TMDBMoviesListCrawler.LATEST_ID.id
		val latest_parsed = getHighestParsed
		log.info(s"Latest parsed: $latest_parsed")
		log.info(s"Latest id is: $latest_id")

		for (id <- latest_parsed to latest_id) {
			log.info(s"$id")
			val (_, needsDownloading) = getFile(TMDBMoviesListCrawler.MOVIE_URL.format(id.toString))
			Thread.sleep(500)
		}

	}

	def determineFileName(uri: URIBuilder): File = {
		val urlSplit = uri.getPath.split('/')
		val queryType  = urlSplit(2)
		val id  = urlSplit(3)


		new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/$queryType/$id")
	}

	def getHighestParsed() = {
		val moviesListDir = new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/movie/")
		// get list of id files
		val moviesList = moviesListDir.list().filter(isAllDigits).map(x => x.toLong)
		moviesList.sorted.reverse.head
	}

	def isAllDigits(x: String) = x forall Character.isDigit

	/**
	 * Downloads a file to the local hard-disk if it has content.
	 * @param file Where to store the file.
	 * @return The saved file on the hard-disk.
	 */

	override def downloadFile(url: URL, file: File): File = {
		// create folders in case they don't exist yet
		file.getParentFile.mkdirs()

		val path = url.toString
		lazy val response = TMDBMoviesListCrawler.movieRequest(path)
		val responseCode = response.responseCode

		if (responseCode == 200) {
			val bw = new BufferedWriter(new FileWriter(file))
			bw.write(response.asString)
			bw.close()
			log.info(s"$path written")
		} else if (responseCode != 404) {
			log.info(s"$path had response code $responseCode")
		}

		return file
	}

}