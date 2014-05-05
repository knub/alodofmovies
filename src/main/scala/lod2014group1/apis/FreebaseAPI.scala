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
	  print (json)
  }

  
  
}
