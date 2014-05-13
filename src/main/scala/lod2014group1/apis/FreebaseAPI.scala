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
import java.io.BufferedReader
import java.io.FileReader
import scala.io.Source
import com.google.api.client.http.HttpResponse

case class FreebaseFilm (name : String, imdb_id: String, id: String)
case class Result (result: List[FreebaseFilm])
case class Ids (mid: String)
case class FilmIds (result: List[Ids], cursor: Option[String])

object FreebaseAPI {
	private val conf = ConfigFactory.load();
	private val API_KEY = conf.getString("alodofmovies.api.key.freebase")
	private val movieListFile = new File(Config.DATA_FOLDER + "/Freebase/movieList.txt")
	
}

class FreebaseAPI{
	private val conf = ConfigFactory.load();
	private val FREEBASE_API_KEY = conf.getString("alodofmovies.api.key.freebase")
	private val movieListFile = new File(Config.DATA_FOLDER + "/Freebase/movieList.txt")
	
	
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
	
	def loadAllFilmId(): Unit = {
		
		implicit val formats = net.liftweb.json.DefaultFormats
		println(FreebaseAPI.API_KEY)
		println(FREEBASE_API_KEY)
		val query = """[{"mid": null, "type": "/film/film", "limit": 400}]"""
		var cursor: Option[String] = Some("")
			
		movieListFile.getParentFile.mkdirs()
		val bw = new BufferedWriter(new FileWriter(movieListFile))
				
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

	def downloadResources() = {
		val movieDirPath = Config.DATA_FOLDER + "/Freebase/MovieJson/"
		val movieDir = new File(movieDirPath)
		movieDir.mkdirs()
		
		for(line <- Source.fromFile("myfile.txt").getLines.take(10)){
			val filePath = new File (movieDirPath + "line")
			if (!filePath.exists()){
				val bw = new BufferedWriter(new FileWriter(filePath))
				bw.write(loadResource(line).toString())	
			}
		}	
		
	}
	
	def loadResource(resource: String): HttpResponse = {
		
		val httpTransport = new NetHttpTransport()
		val requestFactory = httpTransport.createRequestFactory()
		val url = new GenericUrl("https://www.googleapis.com/freebase/v1/topic" + resource)
		url.put("key", FREEBASE_API_KEY)
		url.put("filter", "allproperties")
		val request = requestFactory.buildGetRequest(url);
		request.execute()
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
