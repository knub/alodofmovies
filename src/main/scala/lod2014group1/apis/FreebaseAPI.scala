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

case class FreebaseFilm (name : String, imdb_id: String, id: String)
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
  
  def getImdbQuery(imdbIds: List[String]): String = {
  	val imdbIdsWithQuotes = imdbIds.map(id => "\"" + id + "\"")
  	val imdbString = imdbIdsWithQuotes.mkString(",")
  	
  	
  	"""[{"type": "/film/film", "name": null, "imdb_id|=": [%s],"imdb_id": null, "id" : null }]""".format(imdbString)
  }
  
  def getFreebaseFilmsWithIMDB: Unit = {
  	implicit val formats = net.liftweb.json.DefaultFormats
  	
  	getImdbdsFromDirectories.toList.grouped(5).take(2).foreach{imdbIds =>	
 		val films = requestMQL(getImdbQuery(imdbIds)).extract[Result]
 		films.result.foreach(film => println(new RdfResource("http://www.freebase.com" + film.id).sameAs("http://www.imdb.com/title/" + film.imdb_id)) )
 		//println(films)
  	}
  }

  def getImdbdsFromDirectories: Array[String] = {
  	new File(s"${Config.DATA_FOLDER}/${ImdbMovieCrawler.BASE_DIR_NAME}").list().filterNot(name => name.contains("."))
  }
  
}
