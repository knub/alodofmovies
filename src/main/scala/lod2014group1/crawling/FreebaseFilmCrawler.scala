package lod2014group1.crawling

import com.typesafe.config.ConfigFactory
import lod2014group1.Config
import java.io.File
import org.slf4s.Logging
import java.io.BufferedReader
import java.io.FileReader
import org.apache.http.client.utils.URIBuilder
import java.net.URL
import java.nio.channels.Channels
import java.io.FileOutputStream
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.GenericUrl
import lod2014group1.apis.FreebaseAPI


object FreebaseFilmCrawler {
	private val FILM_DIR = FreebaseAPI.BASE_DIR + "/film/"
	private val topicURL = "https://www.googleapis.com/freebase/v1/topic"
}

class FreebaseFilmCrawler extends Crawler with Logging{
	
	def crawl : Unit = {
		val latest_id = 244398
		val latest_parsed = getHighestParsed
		log.info(s"Latest parsed: $latest_parsed")
		log.info(s"Latest id is: $latest_id")

		val br = new BufferedReader(new FileReader(FreebaseAPI.movieListFile));
		for( a <- 1 to latest_id){
			if (a <= latest_parsed){
				br.readLine
			}else {
				val movieId =  FreebaseFilmCrawler.topicURL + br.readLine
				val (_, needsDownloading) = getFile(movieId)
				if (! needsDownloading) {
				//log.info(s"$id")
				}
				if ((a % 100)==0){
					log.info(s"downloaded movies: $a / $latest_id")
				}
				
			}
		}
		
		br.close()
	}
	
	def getHighestParsed() = {
		val moviesListDir = new File(FreebaseFilmCrawler.FILM_DIR)
		moviesListDir.getParentFile.mkdirs()
		// get list of id files
		val movies = moviesListDir.list()
		if (movies!=null){
			movies.size
		} else {
			0
		}
	}
	
	def determineFileName(uri: URIBuilder): File = {
		val filename = uri.toString().split('/').last
		new File(s"${FreebaseFilmCrawler.FILM_DIR}${filename}")		
	}
	
	override def downloadFile(url: URL, file: File): File = {
		file.getParentFile.mkdirs()
		val response = loadResource(url.toString())
		val responseCode = response.getStatusCode()
		
		val channel = Channels.newChannel(response.getContent())
		val fos = new FileOutputStream(file)
		fos.getChannel().transferFrom(channel, 0, Long.MaxValue)
		file
	}
	
	
	def loadResource(urlString: String): HttpResponse = {
		//"https://www.googleapis.com/freebase/v1/topic"
		
		val httpTransport = new NetHttpTransport()
		val requestFactory = httpTransport.createRequestFactory()
		val url = new GenericUrl(urlString)
		url.put("key", FreebaseAPI.API_KEY)
		url.put("filter", "allproperties")
		val request = requestFactory.buildGetRequest(url);
		request.execute()
	}

}