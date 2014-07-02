package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import java.io.File
import lod2014group1.{Config, I}
import org.apache.commons.io.FileUtils
import lod2014group1.crawling.ImdbMovieCrawler
import scala.collection.JavaConversions._
import org.slf4s.Logging
import scala.io.Codec
import java.nio.charset.CodingErrorAction
import scala.io.Source

class TriplifyDistributor {

	def triplify(file: File): List[RdfTriple] = {
		val content = FileUtils.readFileToString(file, "UTF-8")
		triplify(file.getPath.replace("data/", ""), content)
	}
	def triplify(fileName: String, content: String): List[RdfTriple] = {
		val f = new File(s"${Config.DATA_FOLDER}/$fileName")

		if (fileName.contains("IMDBMovie")) {
			val imdbId = fileName.split("/")(1)
			if (f.getName == "fullcredits.html")
				return new ImdbCastTriplifier(imdbId).triplify(content)
			else if (f.getName == "locations.html")
				return new ImdbLocationTriplifier(imdbId).triplify(content)
			else if (f.getName == "keywords.html")
				return new ImdbKeywordTriplifier(imdbId).triplify(content)
			else if (f.getName == "awards.html")
				return new ImdbAwardTriplifier(imdbId).triplify(content)
			else if (f.getName == "releaseinfo.html")
				return new ImdbReleaseInfoTriplifier(imdbId).triplify(content)
			else if (f.getName == "main.html")
				return new ImdbMainPageTriplifier(imdbId).triplify(content)

		} else if (fileName.contains("Actor")) {
			val imdbId = fileName.split("/")(1)
			if (f.getName == "main.html")
				return new ImdbActorTriplifier(imdbId).triplify(content)

		} else if (fileName.contains("OFDB/Movies")){
			return new OfdbTriplifier().triplify(content)
		} else if (fileName.contains("Freebase")){
			val triplifier = new FreebaseFilmsTriplifier()
			return triplifier.triplify(content)
		}
	throw new RuntimeException(s"Could not find triplifier for file $fileName.")
	} 
}

object TriplifyDistributor extends Logging {
	def go() {
		val triplifier = new TriplifyDistributor
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
				(new FreebaseFilmsTriplifier).triplify(new File("data/Freebase/film/0nd2mj_"))
				//List()
			case Config.Person.Dominik =>
				val tmdbTriplifier = new TmdbMovieTriplifier()
				val tmdbFiles = new File("data/TMDBMoviesList/movie/").listFiles().filter( f => f.getName.endsWith(".json") && f.getName.startsWith("8"))
				log.info(s"Number of movies: ${tmdbFiles.size}")
				//val tmdbFiles = List(new File("data/TMDBMoviesList/movie/550.json"))
				//val tmdbFiles = List(new File("data/TMDBMoviesList/movie/13.json"))
				val part: List[RdfTriple] = tmdbFiles.flatMap( f => tmdbTriplifier.triplify(f)).toList
				//println(part)
				part
			case Config.Person.Tim =>
				var ofdbTriples: List[RdfTriple] = List()
				implicit val codec = Codec("UTF-8")
				codec.onMalformedInput(CodingErrorAction.REPLACE)
				codec.onUnmappableCharacter(CodingErrorAction.REPLACE)
	
				for (ofdbId <- 10166 to 10166){
					val ofdbTriplifier = new OfdbTriplifier()
					val filmPath = s"${Config.DATA_FOLDER}/OFDB/Movies/$ofdbId/Film.html"
					val castPath = s"${Config.DATA_FOLDER}/OFDB/Movies/$ofdbId/Cast.html"
					val filmString = Source.fromFile(filmPath)(codec).mkString
					ofdbTriples = ofdbTriplifier.triplify(filmString) ::: ofdbTriples
					val castString = Source.fromFile(castPath)(codec).mkString
					ofdbTriples = ofdbTriplifier.triplify(castString) ::: ofdbTriples
					if(ofdbId % 10 == 0) 	println(s"$ofdbId Ofdb Movies triplified.")
				}
				ofdbTriples
				//List()
		}

		triples.foreach(println)
	}
}
