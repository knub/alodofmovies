package lod2014group1.apis

import com.google.api.client.http.GenericUrl
import net.liftweb.json.JsonAST.JValue
import com.google.api.client.http.javanet.NetHttpTransport
import net.liftweb.json.JsonParser
import java.io.InputStreamReader
import java.io.File
import lod2014group1.Config
import net.liftweb.json.JsonAST.JArray
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import com.google.api.services.freebase.FreebaseRequest
import java.io.BufferedWriter
import java.io.FileWriter
import com.typesafe.config.ConfigFactory
import com.google.api.client.http.HttpResponse
import lod2014group1.crawling.ImdbMovieCrawler

case class FreebaseFilm (name : String, imdb_id: String, id: String)
case class Result (result: List[FreebaseFilm])
case class Ids (mid: String)
case class FilmIds (result: List[Ids], cursor: Option[String])

object FreebaseAPI {
	val conf = ConfigFactory.load();
	val API_KEY = conf.getString("alodofmovies.api.key.freebase")
	val BASE_DIR = Config.DATA_FOLDER + "/Freebase/"
	val movieListFile = new File(BASE_DIR +"/movieList.txt")
	val actorListFile = new File(BASE_DIR +"/actorList.txt")
}

class FreebaseAPI{
		
	/////////////////////////////////////////////////////////////////
	def requestMQL(mqlQuery: String, cursor: String) : JValue = {

      val httpTransport = new NetHttpTransport()
      val requestFactory = httpTransport.createRequestFactory()
      val url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread")
      url.put("key", FreebaseAPI.API_KEY)
      url.put("query", mqlQuery)
      url.put("cursor", cursor);
      val request = requestFactory.buildGetRequest(url);
      val response = request.execute()
      JsonParser.parse(new InputStreamReader(response.getContent()))
	}

	def getAllNotImdbMovies(): Unit = {
	  val query = "[{\"type\": \"/film/film\", \"imdb_id\": [{\"type\": null, \"optional\": \"forbidden\"}],\"return\": \"count\"}]"
	  val json = requestMQL(query, "")
	  println (json)
	}
	
	def loadAllFilmId(): Unit = {
		
		val query = """[{"mid": null, "type": "/film/film", "limit": 400}]"""
	
		loadIdsWithPagingAndSave(query, FreebaseAPI.movieListFile)
	}
	
	def loadAllActorIds(): Unit = {
		val query = """[{"type": "/film/actor","mid": null, "limit": 400}]"""
		loadIdsWithPagingAndSave(query, FreebaseAPI.actorListFile)
	}
	
	
	def loadIdsWithPagingAndSave(query:String, saveFile: File): Unit = {
		
		implicit val formats = net.liftweb.json.DefaultFormats
		var cursor: Option[String] = Some("")
			
		saveFile.getParentFile.mkdirs()
		val bw = new BufferedWriter(new FileWriter(saveFile))
				
		while (cursor.isDefined){
			val json = requestMQL(query, cursor.get)	
			val filmIds = json.extract[FilmIds]
			println(filmIds.result.size)
			cursor = filmIds.cursor
			println(filmIds.result(1))
			println(cursor)	
		
			writeIdsInFile(filmIds.result, bw)
		}	
	
		bw.close()

	}
	
	def writeIdsInFile(ids:List[Ids], bw: BufferedWriter): Unit = {
		ids.foreach(id => {bw.write(id.mid); bw. write("\r\n")})
	}
  
	def getImdbQuery(imdbIds: List[String]): String = {
		val imdbIdsWithQuotes = imdbIds.map(id => "\"" + id + "\"")
		val imdbString = imdbIdsWithQuotes.mkString(",")
  	
		"""[{"type": "/film/film", "name": null, "imdb_id|=": [%s],"imdb_id": null, "id" : null }]""".format(imdbString)
	}
  
	def getFreebaseFilmsWithIMDB: Unit = {
		implicit val formats = net.liftweb.json.DefaultFormats
  	
		getImdbIdsFromDirectories.toList.grouped(5).take(2).foreach{imdbIds =>	
 			val films = requestMQL(getImdbQuery(imdbIds), "").extract[Result]
 			films.result.foreach(film => println(new RdfResource("http://www.imdb.com/title/" + film.imdb_id).sameAs("http://www.freebase.com" + film.id)) )
 			//println(films)
		}
	}

	def getImdbIdsFromDirectories: Array[String] = {
		new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}").list().filterNot(name => name.contains("."))
	}
}
