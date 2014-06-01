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
import scala.compat.Platform

case class TmdbJsonResponse(id: Long, imdb_id: String, original_title: String)

object TMDBMoviesListCrawler {
	val BASE_DIR_NAME = "TMDBMoviesList"
	val API_INTERVAL: Long = 10000


	val TMDB_API_KEY = IOUtils.toString(new FileInputStream(Config.TMDB_API_KEY))
	val MOVIE_URL  =  "https://api.themoviedb.org/3/movie/%s"
	val ADDITIONAL_MOVIE_INFOS = "credits,keywords,images,videos,alternative_titles,releases,similar"
	val PERSON_URL  =  "https://api.themoviedb.org/3/person/%s"
	val ADDITIONAL_PERSON_INFOS = "external_ids,images,combined_credits"

	implicit val formats = net.liftweb.json.DefaultFormats
	lazy val LATEST_ID = movieRequest(MOVIE_URL.format("latest")) { inputStream => JsonParser.parse(
				new InputStreamReader(inputStream))}.extract[TmdbJsonResponse]

	def movieRequest(url: String): Http.Request = {
		Http(url).options(HttpOptions.connTimeout(5000),
			HttpOptions.readTimeout(10000)).header("retry-after", "10").params("api_key" -> TMDB_API_KEY, "append_to_response" -> ADDITIONAL_MOVIE_INFOS)
	}

	def personRequest(url: String): Http.Request = {
		Http(url).options(HttpOptions.connTimeout(5000),
			HttpOptions.readTimeout(10000)).header("retry-after", "10").params("api_key" -> TMDB_API_KEY, "append_to_response" -> ADDITIONAL_PERSON_INFOS)
	}

}

class TMDBMoviesListCrawler extends Crawler with Logging{

	def crawl: Unit = {
		val latest_id = TMDBMoviesListCrawler.LATEST_ID.id
		val latest_parsed = getHighestParsed
		log.info(s"Latest parsed: $latest_parsed")
		log.info(s"Latest id is: $latest_id")

		var lastTime = Platform.currentTime

		for (id <- latest_parsed to latest_id) {
			val (_, needsDownloading) = getFile(TMDBMoviesListCrawler.MOVIE_URL.format(id.toString))
			if (! needsDownloading) {
				//log.info(s"$id")
			}
			if (id % 20 == 0) {
				Thread.sleep(calcSleep(lastTime))
				lastTime = Platform.currentTime
			}

		}

	}

	def determineFileName(uri: URIBuilder): File = {
		val urlSplit = uri.getPath.split('/')
		val queryType  = urlSplit(2)
		val id  = urlSplit(3)


		new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/$queryType/$id.json")
	}

	def getHighestParsed() = {
		val moviesListDir = new File(s"${Config.DATA_FOLDER}/${TMDBMoviesListCrawler.BASE_DIR_NAME}/movie/")
		// get list of id files
		val moviesList = moviesListDir.list().filter(isAllDigits).map(x => x.toLong)
		moviesList.sorted.reverse.head
	}

	def isAllDigits(x: String) = x forall Character.isDigit

	def calcSleep(lastTime: Long): Long = {
		val timeDiff = (lastTime + TMDBMoviesListCrawler.API_INTERVAL) - Platform.currentTime
		if (timeDiff > 0) {
			log.info(s"sleeping $timeDiff ms")
			timeDiff
		} else {
			0
		}
	}

	/**
	 * Downloads a file to the local hard-disk if it has content.
	 * @param file Where to store the file.
	 * @return The saved file on the hard-disk.
	 */

	override def downloadFile(url: URL, file: File): File = {
		// create folders in case they don't exist yet
		file.getParentFile.mkdirs()

		val path = url.toString

		val response =
		if (file.getParent.equals("movie")) {
			TMDBMoviesListCrawler.movieRequest(path)
		} else if (file.getParent.equals("person")) {
			TMDBMoviesListCrawler.personRequest(path)
		} else {
			log.error (s"No valid file: ${file.toString}")
			return file
		}

		val responseCode = response.responseCode

		if (responseCode == 200) {
			saveResponse(file, response.asString)
			log.info(s"Downloaded: $path")
		} else if (response.responseCode != 404) {
			log.error(s"$path had response code $responseCode")
		} else {
			log.info(s"Page not found: $path")
		}

		return file
	}



	def saveResponse(file: File, response: String) {
		val bw = new BufferedWriter(new FileWriter(file))
		bw.write(response)
		bw.close()
	}

}