package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import lod2014group1.{Config, I}
import org.apache.commons.io.FileUtils
import lod2014group1.crawling.ImdbMovieCrawler
import scala.collection.JavaConversions._
import org.slf4s.Logging

class Triplifier {

	def triplify(file: File): List[RdfTriple] = {
		val content = FileUtils.readFileToString(file, "UTF-8")
		triplify(file.getPath.replace("data/", ""), content)
	}
	def triplify(fileName: String, content: String): List[RdfTriple] = {
		val f = new File(s"${Config.DATA_FOLDER}/$fileName")

		if (fileName.contains("IMDBMovie")) {
			val imdbId = fileName.split("/")(1)
			if (f.getName == "fullcredits.html")
				new ImdbCastTriplifier(imdbId).triplify(content)
			else if (f.getName == "locations.html")
				new ImdbLocationTriplifier(imdbId).triplify(content)
			else if (f.getName == "keywords.html")
				new ImdbKeywordTriplifier(imdbId).triplify(content)
			else if (f.getName == "awards.html")
				new ImdbAwardTriplifier(imdbId).triplify(content)
			else if (f.getName == "releaseinfo.html")
				new ImdbReleaseInfoTriplifier(imdbId).triplify(content)
			else if (f.getName == "main.html")
				new ImdbMainPageTriplifier(imdbId).triplify(content)
			else
				List()
		} else if (fileName.contains("Actor")) {
			val imdbId = fileName.split("/")(1)
			println(fileName)
			if (f.getName == "main.html")
				new ImdbActorTriplifier(imdbId).triplify(content)
			else
				List()
		} else {
			List()
		}
	}
}

object Triplifier extends Logging {
	def go() {
		val triplifier = new Triplifier
		val triples = I.am match {
			case Config.Person.Stefan =>
				triplifier.triplify(new File("data/IMDBMovie/tt0109830/fullcredits.html"))
			case Config.Person.Tanja =>
				triplifier.triplify(new File("data/IMDBMovie/tt0179184/main.html"))
			case Config.Person.Rice =>
				
				var i = 0
				val files = new File("data/Freebase/film/").listFiles()
//				files.take(3).flatMap{ file =>
//					val id = s"/m/${file.getName}"
//					//println(id)
//					i+=1
//					if (i %1000 == 0) println(i) 
//					new FreebaseFilmsTriplifier(id).triplify(file)
//				}.toList
				//new FreebaseFilmsTriplifier("/m/0bdjd").triplify(new File("data/Freebase/film/0bdjd")) ::: 
				new FreebaseFilmsTriplifier("/m/047csmy").triplify(new File("data/Freebase/film/047csmy"))
				//List()
			case Config.Person.Dominik =>
				val tmdbTriplifier = new TmdbMovieTriplifier()
				//val tmdbFiles = new File("data/TMDBMoviesList/movie/").listFiles().filter( f => f.getName.endsWith(".json"))
				//log.info(s"Number of movies: ${tmdbFiles.size}")
				//val tmdbFiles = List(new File("data/TMDBMoviesList/movie/550.json"))
				val tmdbFiles = List(new File("data/TMDBMoviesList/movie/13.json"))
				val part: List[RdfTriple] = tmdbFiles.flatMap( f => tmdbTriplifier.triplify(f)).toList
				//println(part)
				part
			case Config.Person.Tim =>
				var ofdbTriples: List[RdfTriple] = List()
				for (i <- 1 to 100000){
					ofdbTriples = new OfdbTriplifier(i).triplify() ::: ofdbTriples
					if(i % 100 == 0)
						println(s"$i Ofdb Movies triplified.")
				}
				ofdbTriples
		}

		triples.foreach(println)
	}
}
