package lod2014group1.crawling

import org.apache.http.client.utils.URIBuilder
import java.io.File
import org.apache.commons.io.FileUtils
import lod2014group1.Config
import lod2014group1.Config.Person._
import lod2014group1.I
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import scala.io.Source
import java.nio.charset.CodingErrorAction
import scala.io.Codec
import java.nio.file.Files
import java.nio.file.Paths


object OFDBMovieCrawler {
	val OFDB_PATH = s"${Config.DATA_FOLDER}/OFDB"
}

class OFDBMovieCrawler extends Crawler {
	
	def coverage: Unit = {
		implicit val codec = Codec("UTF-8")
		codec.onMalformedInput(CodingErrorAction.REPLACE)
		codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
		val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
		if (!Files.exists(Paths.get(lastMovieFilePath)))
			println(s"$lastMovieFilePath not found. Could not compute coverage.")
		else{
			val lastMovieIDCrawled = Source.fromFile(lastMovieFilePath)(codec).mkString.toInt
			println(s"Last movie ID crawled: $lastMovieIDCrawled.")
			val movieBasePath = s"${OFDBMovieCrawler.OFDB_PATH}/Movies"
			var imdbLinks = 0
			val statusPrintEvery = 1000
			var moviesFound = 0
			for (i <- 1 to lastMovieIDCrawled){
				val currentMoviePath = s"$movieBasePath/${i.toString}/Film.html"
				if (Files.exists(Paths.get(currentMoviePath))){
					val fileContent = Source.fromFile(currentMoviePath)(codec).mkString
					if (fileContent.contains("<head>")){
						if (fileContent.contains("www.imdb.com"))
							imdbLinks = imdbLinks + 1
						moviesFound = moviesFound + 1
					}
				}
				if (i % statusPrintEvery == 0){
					var coveragePercent = 100.0f * imdbLinks.toFloat / moviesFound.toFloat
					println(s"Processed $moviesFound movies. IMBd coverage so far: $imdbLinks -> $coveragePercent%.")
				}
			}
			val existingMoviesPercent = 100.0f * moviesFound.toFloat / lastMovieIDCrawled.toFloat
			println(s"\r\n\r\nOf $lastMovieIDCrawled movie IDs crawled, $moviesFound ectually exist. That's $existingMoviesPercent%.")
			val coveragePercent = 100.0f * imdbLinks.toFloat / moviesFound.toFloat
			println(s"\r\nOf $moviesFound movies already crawled, $imdbLinks have Links to IMDb. That's $coveragePercent%.\r\n\r\n")
		}
	}

	def crawl: Unit = {
	println("\r\n\r\n")
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val errorLogFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBErrorLog.txt"
	if (!Files.exists(Paths.get(errorLogFilePath)))
		scala.tools.nsc.io.File(errorLogFilePath).writeAll("")
	val nextMovieDelayInSeconds = 10
	val waitWhenUnavailableInSeconds = 3600
	val maxReloadAttempts = 10
	val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
	if (!Files.exists(Paths.get(lastMovieFilePath)))
		scala.tools.nsc.io.File(lastMovieFilePath).writeAll("0")
	val lastMovieIDCrawled = Source.fromFile(lastMovieFilePath)(codec).mkString.toInt
	println(s"Last movie ID crawled: $lastMovieIDCrawled.")

	val OFDBStatsURL = "http://www.ofdb.de/view.php?page=stats"
	val uriBuilderStats = new URIBuilder(OFDBStatsURL)
	downloadFile(uriBuilderStats.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBStats.html"))
	val StatisticsPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBStats.html").mkString
	val tempStats = StatisticsPage.split("<font color=\"#005500\">")(1).split("<br>")(0).split("\\.")
	val numberOfMoviesInOFDB = (tempStats(0) + tempStats(1)).toInt
	println(s"Total number of movies in OFDB: $numberOfMoviesInOFDB.")

	val OFDBNewMoviesURL = "http://www.ofdb.de/view.php?page=neu&Kat=Film&Tage=1"
	val uriBuilderNew = new URIBuilder(OFDBNewMoviesURL)
	downloadFile(uriBuilderNew.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBNewMovies.html"))
	val newMoviesPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBNewMovies.html")(codec).mkString
	val newMoviesStrings = newMoviesPage.split("<a href=\"film/")
	var lastIDToCrawl = numberOfMoviesInOFDB;
	for (i <- 1 to newMoviesStrings.length - 1){
		val newMovieString = newMoviesStrings(i) 
		val movieIDString = newMovieString.split(",")(0)
		val movieID = movieIDString.toInt
		if (movieID > lastIDToCrawl)
							lastIDToCrawl = movieID
			}
	println(s"Last ID to crawl: $lastIDToCrawl.")


	val CastURLBase  =  "http://www.ofdb.de/view.php?page=film_detail&fid="
	val FilmURLBase = "http://www.ofdb.de/"
	var currentMovieID = lastMovieIDCrawled + 1
	for (currentMovieID <- lastMovieIDCrawled + 1 to lastIDToCrawl){
		try{
			val currentCastURL = CastURLBase + currentMovieID
			val uriBuilderCast = new URIBuilder(currentCastURL)
			downloadFile(uriBuilderCast.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Cast.html"))
			val castPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Cast.html")(codec).mkString
			var loadAttempts = 0
			while (!castPage.contains("<") && loadAttempts < maxReloadAttempts){
				loadAttempts = loadAttempts + 1
				println(s"Page $currentCastURL unavailable. Retrying in $waitWhenUnavailableInSeconds seconds. Attempt $loadAttempts")
				Thread.sleep(waitWhenUnavailableInSeconds * 1000)
				downloadFile(uriBuilderCast.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Cast.html"))
				val castPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Cast.html")(codec).mkString
			}
			if (loadAttempts >= maxReloadAttempts){
				scala.tools.nsc.io.File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Cast.html").writeAll("<html><body>Unter dieser ID existiert kein Film.</body></html>")
				scala.tools.nsc.io.File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html").writeAll("<html><body>Unter dieser ID existiert kein Film.</body></html>")
				val errorFileContent = Source.fromFile(errorLogFilePath)(codec).mkString
				scala.tools.nsc.io.File(errorLogFilePath).writeAll(s"$errorFileContent\r\nCould not load page after $maxReloadAttempts attempts: $currentCastURL\r\n")
			}
			if (castPage.contains("Unter dieser ID existiert kein Film.") || castPage.contains("Anzeige nicht zul") || loadAttempts >= maxReloadAttempts){
				println(s"Movie with ID $currentMovieID does not exist. Moving on to next movie.")
				scala.tools.nsc.io.File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html").writeAll("<html><body>Unter dieser ID existiert kein Film.</body></html>")
			}
			else{
				Thread.sleep((nextMovieDelayInSeconds + 1) * 1000)
				val currentIDAndName = castPage.split("""ck zur Hauptseite" onClick="javascript:document.location='""", 2)(1).split("""';"><br>""", 2)(0)
				val currentFilmURL = FilmURLBase + currentIDAndName.replaceAll("\\\\", "")
				val uriBuilderFilm = new URIBuilder(currentFilmURL)
				downloadFile(uriBuilderFilm.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html"))
				val filmPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html")(codec).mkString
				//var triedOnce = false
				while(!filmPage.contains("<")){
					//if(!triedOnce){
					//	Thread.sleep(nextMovieDelayInSeconds * 1000 * 2)
					//	triedOnce = true
					//}
					//else{
						println(s"Page $currentFilmURL unavailable. Retrying in $waitWhenUnavailableInSeconds seconds.")
						Thread.sleep(waitWhenUnavailableInSeconds * 1000)
					//}
					downloadFile(uriBuilderFilm.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html"))
					val filmPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$currentMovieID/Film.html")(codec).mkString
				}
				println(s"Downloaded: $currentFilmURL")
				scala.tools.nsc.io.File(lastMovieFilePath).writeAll(currentMovieID.toString)
			}
		}
		catch{
			case e : Exception =>{
				val exceptionText = e.getMessage()
				val errorFileContent = Source.fromFile(errorLogFilePath)(codec).mkString
				scala.tools.nsc.io.File(errorLogFilePath).writeAll(s"$errorFileContent\r\nException occured: \r\n$exceptionText \r\n for movie ID $currentMovieID\r\n")
				println()
				println(s"Exception caught: $exceptionText. See error log at $errorLogFilePath")
				println()
			}
		}
		Thread.sleep(nextMovieDelayInSeconds * 1000)
		//if (currentMovieID % 50 == 0)
		//	println(s"Downloaded Movie with ID $currentMovieID.")
	}
}

def determineFileName(url: URIBuilder): File = {
	val urlSplit = url.getPath.split('/')
			val movieId  = urlSplit(2)
			val pageType = if (urlSplit.length > 3) urlSplit(3) + ".html" else "main.html"
				new File(s"${OFDBMovieCrawler.OFDB_PATH}/$movieId/$pageType")
}
}
