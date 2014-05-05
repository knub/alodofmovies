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
import lod2014group1.crawling.IMDBMovieCrawler
import net.liftweb.json.JsonAST.JArray

case class FreebaseFilm (name : String, imdb_id: String)
case class Result (result: List[FreebaseFilm])


class FreebaseAPI extends App{

	val API_KEY = IOUtils.toString(new FileInputStream(Config.FREEBASE_API_KEY))

  def requestMQL(mqlQuery: String) : JValue = {

      val httpTransport = new NetHttpTransport()
      val requestFactory = httpTransport.createRequestFactory()
      val url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread")
      url.put("key", API_KEY)
      url.put("query", mqlQuery)
      val request = requestFactory.buildGetRequest(url);
      val response = request.execute()
      JsonParser.parse(new InputStreamReader(response.getContent()))
  }

  def getAllNotImdbMovies() = {
	  val query = "[{\"type\": \"/film/film\", \"imdb_id\": [{\"type\": null, \"optional\": \"forbidden\"}],\"return\": \"count\"}]"
	  val json = requestMQL(query)
	  println (json)
  }
  
  def getImdbQuery(imdbId: String): String = {
  	"""[{"type": "/film/film", "name": null, "imdb_id": "%s"}]""".format(imdbId)
  }
  
  def getFreebaseFilmsWithIMDB={
  	implicit val formats = net.liftweb.json.DefaultFormats
  	
  	getImdbdsFromDirectories.take(10).foreach(imdbId => {
//  		println(imdbId)
//  		val json = requestMQL(getImdbQuery(imdbId))
//  		println(json)
//  		val filmJson = Result(for {
//  			JArray(films) <- json \ "result"
//  			film <- films
//  		} yield film.extract[FreebaseFilm])
//  		
//  		println(filmJson)
 		
 		val films = requestMQL(getImdbQuery(imdbId)).extract[Result]
 		println(films)
  	})
  }

  def getImdbdsFromDirectories: Array[String] = {
  	new File(s"${Config.DATA_FOLDER}/${IMDBMovieCrawler.BASE_DIR_NAME}").list().filterNot(name => name.contains("."))
  }
  
}
