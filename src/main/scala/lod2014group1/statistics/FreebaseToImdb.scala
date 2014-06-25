package lod2014group1.statistics

import java.io.File
import lod2014group1.crawling.FreebaseFilmCrawler
import java.io.BufferedReader
import java.io.FileReader
import lod2014group1.apis.FreebaseAPI
import lod2014group1.triplification.FreebaseFilmsTriplifier
import org.slf4s.Logging

class FreebaseToImdb extends Logging{
	val logFile = "freebaselogFile.log"
	
	def matchFreebase() = {
		val br = new BufferedReader(new FileReader(FreebaseAPI.movieListFile));
		var id = br.readLine()

		val source = scala.io.Source.fromFile(logFile)
		val lines = source.getLines()
		var processedIds = List[String]()
		lines.foreach(line => processedIds = line.split(":").last :: processedIds)
		source.close()
		
		while (id != null){
			if (!processedIds.contains(id)){
				val file = determineFileName(id)
				val triplifier = new FreebaseFilmsTriplifier()
				triplifier.triplify(file)
			}
			
			id = br.readLine()
		}

		br.close()
		
	}
	
	def determineFileName(uri: String): File = {
		val filename = uri.split('/').last
		new File(s"${FreebaseFilmCrawler.FILM_DIR}${filename}")		
	}
	
	def getStatistic() = {
		val br = new BufferedReader(new FileReader(logFile));
		var logLine = br.readLine()
		var imdbCount = 0
		var equivalentCount = 0
		var noMatch = 0
		var multiplePossibilities = 0
		var wrongIdGiven = 0
		var wikimatch = 0
		
		while (logLine != null){
			if (logLine.contains("imdb")) imdbCount = imdbCount + 1
			else if (logLine.contains("wiki")) wikimatch = wikimatch + 1
			else if (logLine.contains("equivalent")) equivalentCount = equivalentCount + 1	
			else if (logLine.contains("WARN")) multiplePossibilities = multiplePossibilities + 1
			else if (logLine.contains("wrong")) wrongIdGiven = wrongIdGiven + 1

			else noMatch = noMatch + 1
			logLine = br.readLine()
		}
		
		br.close()
		log.info(s"matched by imdb ID: $imdbCount")
		log.info(s"matched by equivalent webpage: $equivalentCount")
		log.info(s"no match: $noMatch")
		log.info(s"multiple matches possible: $multiplePossibilities")
		log.info(s"wrong imdb given: $wrongIdGiven")
		log.info(s"wiki: $wikimatch") 
	}
	
}
