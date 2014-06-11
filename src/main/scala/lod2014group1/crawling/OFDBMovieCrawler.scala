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


// case class Answer ( header : String, files : List[UriFile], triples : List [triplet] )
// case class UriFile (url : String, content : String)
// TODO: Add function to download a range of IDs
// TODO: Delete "TODO Delete" lines
// TODO: Delete OFDBAnswer occurences & class + replace with Answer class
// TODO: Find out how to reply if timeout occurred.


object OFDBMovieCrawler {
	val OFDB_PATH = s"${Config.DATA_FOLDER}/OFDB"
}

class OFDBMovieCrawler extends Crawler {
	
	def clean: Unit = {
		implicit val codec = Codec("UTF-8")
		codec.onMalformedInput(CodingErrorAction.REPLACE)
		codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
		val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
		if (!Files.exists(Paths.get(lastMovieFilePath)))
			println(s"$lastMovieFilePath not found. Could not clean HTML files.")
		else{
			val lastMovieIDCrawledString = Source.fromFile(lastMovieFilePath).mkString
			val lastMovieIDCrawled = lastMovieIDCrawledString.trim().toInt
			println(s"Last movie ID crawled: $lastMovieIDCrawled.")
			val movieBasePath = s"${OFDBMovieCrawler.OFDB_PATH}/Movies"
			for (i <- 9397 to lastMovieIDCrawled){
				val castPath = s"$movieBasePath/$i/Cast.html"
				if (Files.exists(Paths.get(castPath))){
					val castContent = Source.fromFile(castPath)(codec).mkString
					scala.tools.nsc.io.File(castPath).writeAll(cleanOneCast(castContent, codec))
				}
				val filmPath = s"$movieBasePath/$i/Film.html"
				if (Files.exists(Paths.get(filmPath))){
					val filmContent = Source.fromFile(filmPath)(codec).mkString
					scala.tools.nsc.io.File(filmPath).writeAll(cleanOneFilm(filmContent, codec))
				}
				if ( i % 50 == 0) {
					println(s"Cleaned $i movies. ${lastMovieIDCrawled - i} to go.")
				}
			}
		}
	}
	
	
	def cleanOneCast(contentString : String, codec : Codec): String = {
		var fileContent = contentString
		if (fileContent.contains("Unter dieser ID existiert kein Film.") || fileContent.contains("Anzeige nicht zul")){
			return """<html><body>Unter dieser ID existiert kein Film.</body></html>"""
		}
		fileContent = fileContent.split("""ndigkeit.</font></div>\n<br>""", 2)(1)
		fileContent = fileContent.split("""<br><br><br><div align="center"><script type="text/javascript"><!--\ngoogle_ad_client = """", 2)(0)
		fileContent = s"""<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
$fileContent
</body>
</html>"""
		return fileContent
	}
	
	
	def cleanOneFilm(contentString : String, codec : Codec): String = {
		var fileContent = contentString
		if (fileContent.contains("Unter dieser ID existiert kein Film.") || fileContent.contains("Anzeige nicht zul")){
			return "<html><body>Unter dieser ID existiert kein Film.</body></html>"
		}
		fileContent = fileContent.split("""<div itemscope itemtype="http://schema.org/Movie">\n<table cellspacing="0" cellpadding="0" border="0">\n<tr valign="top"><td width="99%">\n<table cellspacing="0" cellpadding="0" border="0">\n<tr valign="top">""", 2)(1)
		fileContent = fileContent.split("""<font face="Arial,Helvetica,sans-serif" size="2" class="Normal"><br>Neue Fassung eintragen:""", 2)(0)
		val fileContentTopPart = fileContent.split("""</td><td width="1%"><div id="Layer_mb" style="position: relative; left: 10px; top: -15px; z-index: 1;"><script src="Scripts/swfobject_modified.js" type="text/javascript"></script>""", 2)(0)
		fileContent = fileContent.split("""<td nowrap><font face="Arial,Helvetica,sans-serif" size="2" class="Normal">Genre""", 2)(1)
		fileContent = s"""<td><font face="Arial,Helvetica,sans-serif">Genre $fileContent"""
		val fileContentMiddlePart = fileContent.split("""<!-- Shop-Artikel darstellen -->""", 2)(0)
		fileContent = fileContent.split("""<td width="99%"><img src="images/design3/s_linie_t.png" width="150" height="10" border="0"><br><div style="color: #0000AA; font-size: 18px; font-family: Arial, Helvetica, sans-serif; font-weight: bold; font-style: italic;">Fassungen</div></td>\n</tr>""", 2)(1)
		fileContent = s"""Fassungen <table> $fileContent"""
		fileContent = s"""<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table>
<tr>
<td>
<table>
<tr>
$fileContentTopPart
$fileContentMiddlePart
$fileContent
</tr>
</table>
</body>
</html>"""
	return fileContent
	}
	
	
	def coverage: Unit = {
		implicit val codec = Codec("UTF-8")
		codec.onMalformedInput(CodingErrorAction.REPLACE)
		codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
		val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
		if (!Files.exists(Paths.get(lastMovieFilePath))){
			println(s"$lastMovieFilePath not found. Could not compute coverage.")
			return
		}
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
				println(s"Processed $moviesFound movies. IMDb coverage so far: $imdbLinks -> $coveragePercent%.")
			}
		}
		val existingMoviesPercent = 100.0f * moviesFound.toFloat / lastMovieIDCrawled.toFloat
		println(s"\r\n\r\nOf $lastMovieIDCrawled movie IDs crawled, $moviesFound ectually exist. That's $existingMoviesPercent%.")
		val coveragePercent = 100.0f * imdbLinks.toFloat / moviesFound.toFloat
		println(s"\r\nOf $moviesFound movies already crawled, $imdbLinks have Links to IMDb. That's $coveragePercent%.\r\n\r\n")
	}

def crawl: Unit = {
	println("\r\n\r\n")
	val errorLogFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBErrorLog.txt"
	if (!Files.exists(Paths.get(errorLogFilePath)))
		scala.tools.nsc.io.File(errorLogFilePath).writeAll("")
	val nextMovieDelayInSeconds = 10
	val waitWhenUnavailableInSeconds = 3600
	val maxReloadAttempts = 10
	val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
	val lastMovieIDCrawled = getLastMovieIDCrawled()
	val numberOfMoviesInOFDB = getNumberOfMoviesInOFDB()
	val lastIDToCrawl = getLastIDToCrawl()
	println(s"Last ID to crawl: $lastIDToCrawl.")
	
	var currentMovieID = lastMovieIDCrawled + 1
	for (currentMovieID <- lastMovieIDCrawled + 1 to lastIDToCrawl){
		crawlIDForBothFiles(currentMovieID)
		scala.tools.nsc.io.File(lastMovieFilePath).writeAll(currentMovieID.toString)
		println(s"Downloaded: $currentMovieID")
		Thread.sleep(nextMovieDelayInSeconds * 1000)
	}
}

def getLastMovieIDCrawled(): Integer = {
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val lastMovieFilePath = s"${OFDBMovieCrawler.OFDB_PATH}/OFDBLastMovie.txt"
	if (!Files.exists(Paths.get(lastMovieFilePath)))
		scala.tools.nsc.io.File(lastMovieFilePath).writeAll("0")
	val lastMovieIDCrawled = Source.fromFile(lastMovieFilePath)(codec).mkString.toInt
	println(s"Last movie ID crawled: $lastMovieIDCrawled.")
	return lastMovieIDCrawled
	}
	
def getNumberOfMoviesInOFDB(): Integer = {
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val OFDBStatsURL = "http://www.ofdb.de/view.php?page=stats"
	val uriBuilderStats = new URIBuilder(OFDBStatsURL)
	downloadFile(uriBuilderStats.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBStats.html"))
	val StatisticsPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBStats.html").mkString
	val tempStats = StatisticsPage.split("<font color=\"#005500\">")(1).split("<br>")(0).split("\\.")
	val numberOfMoviesInOFDB = (tempStats(0) + tempStats(1)).toInt
	println(s"Total number of movies in OFDB: $numberOfMoviesInOFDB.")
	return numberOfMoviesInOFDB
	}

def getLastIDToCrawl(): Integer = {
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val OFDBNewMoviesURL = "http://www.ofdb.de/view.php?page=neu&Kat=Film&Tage=1"
	val uriBuilderNew = new URIBuilder(OFDBNewMoviesURL)
	downloadFile(uriBuilderNew.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBNewMovies.html"))
	val newMoviesPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/OFDBNewMovies.html")(codec).mkString
	val newMoviesStrings = newMoviesPage.split("<a href=\"film/")
	// latest ID as of 2014-05-21 16:15
	var lastIDToCrawl = 263149
	for (i <- 1 to newMoviesStrings.length - 1){
		val newMovieString = newMoviesStrings(i) 
		val movieIDString = newMovieString.split(",")(0)
		val movieID = movieIDString.toInt
		if (movieID > lastIDToCrawl)
			lastIDToCrawl = movieID
		}
	return lastIDToCrawl
}

def crawlIDForBothFiles(movieID : Integer): OFDBCrawlAnswer = {
	implicit val codec = Codec("UTF-8")
	codec.onMalformedInput(CodingErrorAction.REPLACE)
	codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	val nextMovieDelayInSeconds = 10
	val waitWhenUnavailableInSeconds = nextMovieDelayInSeconds * 5
	val maxReloadAttempts = 3
	val CastURLBase  =  "http://www.ofdb.de/view.php?page=film_detail&fid="
	val FilmURLBase = "http://www.ofdb.de/film/"

	val currentCastURL = CastURLBase + movieID
	val uriBuilderCast = new URIBuilder(currentCastURL)
	downloadFile(uriBuilderCast.build().toURL(), new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$movieID/Cast.html"))
	var castPage = Source.fromFile(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$movieID/Cast.html")(codec).mkString
	var filmPage = ""
	var loadAttempts = 0
	val castPath = s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$movieID/Cast.html"
	val filmPath = s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$movieID/Film.html"
	while (!castPage.contains("<") && loadAttempts < maxReloadAttempts){
		loadAttempts = loadAttempts + 1
		println(s"Page $currentCastURL unavailable. Retrying in $waitWhenUnavailableInSeconds seconds. Attempt $loadAttempts")
		Thread.sleep(waitWhenUnavailableInSeconds * 1000)
		downloadFile(uriBuilderCast.build().toURL(), new File(castPath))
		val castPage = Source.fromFile(castPath)(codec).mkString
	}
	if (castPage.contains("Unter dieser ID existiert kein Film.") || castPage.contains("Anzeige nicht zul") || loadAttempts >= maxReloadAttempts){
		println(s"Movie with ID $movieID does not exist. Moving on to next movie.")
		castPage = "<html><body>Unter dieser ID existiert kein Film.</body></html>"
		filmPage = castPage
		scala.tools.nsc.io.File(castPath).writeAll(castPage) //TODO Remove
		scala.tools.nsc.io.File(filmPath).writeAll(filmPage) //TODO Remove
	}
	else{
		castPage = cleanOneCast(castPage, codec)
		scala.tools.nsc.io.File(castPath).writeAll(castPage)  //TODO Remove
		Thread.sleep((nextMovieDelayInSeconds + 1) * 1000)
		val filmURL = s"$FilmURLBase$movieID,"
		val uriBuilderFilm = new URIBuilder(filmURL)
		downloadFile(uriBuilderFilm.build().toURL(), new File(filmPath))
		var filmPage = Source.fromFile(filmPath)(codec).mkString
		while(!filmPage.contains("<")){
			println(s"Page filmURL unavailable. Retrying in $waitWhenUnavailableInSeconds seconds.")
			Thread.sleep(waitWhenUnavailableInSeconds * 1000)
			downloadFile(uriBuilderFilm.build().toURL(), new File(filmPath))
			val filmPage = Source.fromFile(filmPath)(codec).mkString
		}
		filmPage = cleanOneFilm(filmPage, codec)
		scala.tools.nsc.io.File(filmPath).writeAll(filmPage) // TODO Remove
	}
	val answer = OFDBCrawlAnswer(filmPage, castPage)
	return answer
}

def downloadPageFromURL(url : String): String = {
	val fileString = ""
	return fileString
}

def determineFileName(url: URIBuilder): File = {
	val urlSplit = url.getPath.split('/')
	var movieId = ""
		var pageType = ""
	if (urlSplit(1) == "film") {
		movieId = urlSplit(2).split(",")(0)
		pageType = "Film.html"
	}
	else {
		movieId = urlSplit(2).split("=")(2)
		pageType = "Cast.html"
	}
	new File(s"${OFDBMovieCrawler.OFDB_PATH}/Movies/$movieId/$pageType")
}


}
