package lod2014group1.apis

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import java.io.FileInputStream
import java.util.Properties
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.CloseableHttpResponse

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;

class FreebaseAPI extends App{
  
  val API_KEY = "AIzaSyAJyCWH58NBZqb5hVmXrDa1En0OzpFj9ls"

  def requestMQL(mqlQuery: String) : HttpResponse = {

      val httpTransport = new NetHttpTransport()
      val requestFactory = httpTransport.createRequestFactory()
      val url = new GenericUrl("https://www.googleapis.com/freebase/v1/mqlread")
      url.put("key", API_KEY)
      url.put("query", mqlQuery)
      val request = requestFactory.buildGetRequest(url);
      request.execute()
  }

  def getAllNotImdbMovies() = {
	  val query = "[{\"type\": \"/film/film\", \"imdb_id\": [{\"type\": null, \"optional\": \"forbidden\"}],\"return\": \"count\"}]"
	  val response = requestMQL(query)
	  System.out.println(response.getHeaders())
	  System.out.println(response.getStatusCode())
  }
  
//  def requestRdf(topicId: String):CloseableHttpResponse = {
//    
//    val serviceURL = "https://www.googleapis.com/freebase/v1/rdf"
//    val httpclient = new DefaultHttpClient()
//    
//    val url = serviceURL + topicId + "?key=" + API_KEY
//    val httprequest = new org.apache.http.client.methods.HttpGet(url)
//    httpclient.execute(httprequest)
//  }
//  
  def getExampleRdf() = {
    val topicId = "/en/100_girls"
 //   val response = requestRdf(topicId)
 //   System.out.println(response); 
  }
  
}
