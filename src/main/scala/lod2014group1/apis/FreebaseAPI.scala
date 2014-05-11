package lod2014group1.apis

import com.google.api.client.http.GenericUrl
import net.liftweb.json.JsonAST.JValue
import com.google.api.client.http.javanet.NetHttpTransport
import net.liftweb.json.JsonParser
import java.io.InputStreamReader
import java.io.File
import java.io.FileInputStream
import org.apache.commons.io.IOUtils
import lod2014group1.Config
import lod2014group1.crawling.ImdbMovieCrawler
import net.liftweb.json.JsonAST.JArray
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import com.google.api.services.freebase.FreebaseRequest
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.FileWriter
import com.typesafe.config.ConfigFactory

case class FreebaseFilm (name : String, imdb_id: String, id: String)
case class Result (result: List[FreebaseFilm])
case class Ids (id: String)
case class FilmIds (result: List[Ids], cursor: String)

object FreebaseAPI {
	private val conf = ConfigFactory.load();
	private val API_KEY = conf.getString("alodofmovies.api.key.freebase")
}

class FreebaseAPI{
	private val conf = ConfigFactory.load();
	private val FREEBASE_API_KEY = conf.getString("alodofmovies.api.key.freebase")
	
	def requestMQL(mqlQuery: String, cursor: String) : JValue = {

      val httpTransport = new NetHttpTransport()
      val requestFactory = httpTransport.createRequestFactory()
      val url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread")
      url.put("key", FREEBASE_API_KEY)
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
	
	def getAllFilmId(): Unit = {
		implicit val formats = net.liftweb.json.DefaultFormats
		println(FreebaseAPI.API_KEY)
		println(FREEBASE_API_KEY)
		val query = """[{"id": null, "type": "/film/film", "limit": 400}]"""
		var cursor = ""
		val movieListFile = new File(Config.DATA_FOLDER + "/Freebase/movieList.txt")
		movieListFile.getParentFile.mkdirs()
		val bw = new BufferedWriter(new FileWriter(movieListFile))
			
		while (cursor!= null){
			val json = requestMQL(query, cursor)	
			val filmIds = json.extract[FilmIds]
			println(filmIds.result.size)
			cursor = filmIds.cursor
			println(filmIds.result(1))
			println(cursor)	
		
			writeIdsInFile(filmIds.result, bw)
			Thread.sleep(1000)
		}	

		bw.close()
	}
	
	def writeIdsInFile(ids:List[Ids], bw: BufferedWriter): Unit = {
		ids.foreach(id => {bw.write(id.id); bw. write("\r\n")})
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
 			films.result.foreach(film => println(new RdfResource("http://www.freebase.com" + film.id).sameAs("http://www.imdb.com/title/" + film.imdb_id)) )
 			//println(films)
		}
	}

	def getImdbIdsFromDirectories: Array[String] = {
		new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}").list().filterNot(name => name.contains("."))
	}
}
