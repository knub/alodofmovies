package lod2014group1.triplification

import java.io.File
import lod2014group1.rdf.RdfTriple
import net.liftweb.json.JsonParser
import java.io.FileReader
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfMovieResource
import org.slf4s.Logging
import org.slf4s.Logger
import org.slf4s.LoggerFactory
import lod2014group1.rdf.RdfTriple

class FreebaseFilmsTriplifier(val freebaseId: String) extends Logging {
	
	val FREEBASE_URI = "www.freebase.com"

	protected val fileLog: Logger = LoggerFactory.getLogger("FreebaseFileLogger")
	
	def triplify(f: File): (List[RdfTriple], Int, Int)= {
		
		var triples: List[RdfTriple] = List()
		
		implicit val formats = net.liftweb.json.DefaultFormats
		val json = JsonParser.parse(new FileReader(f))
		
		val topicEquivalentWebpageJson = json\\"/common/topic/topic_equivalent_webpage"\"values"\"value"
		val topicEquivalentWebpages = topicEquivalentWebpageJson.extract[List[String]]
		
		var imdbFlag = 0
		var equivFlag = 0
		
		var imdbId = getImdbIdFromImdbTag(json)
		if (imdbId.isEmpty) imdbId = getImdbIdFromWebpages(topicEquivalentWebpages)
		else imdbFlag = imdbFlag + 1
		
		var id = ""
		
		imdbId match {
			case Some(imdbId) => {
					triples = RdfMovieResource.fromImdbId(imdbId).sameAs(FREEBASE_URI + freebaseId) :: triples// 
					equivFlag = equivFlag + 1
					id = imdbId
				}
			case None => id = idFromFreebaseId()
		}
		
		(triples, imdbFlag, equivFlag)
	}
	
	
	def getImdbIdFromImdbTag(json: JValue): Option[String] = {
		implicit val formats = net.liftweb.json.DefaultFormats
			val rawImdb = json\"/imdb/topic/title_id"\"values"\"value"
			val imdb = rawImdb.extract[List[String]]
			if (imdb.isEmpty)
				None
			else Some (imdb.head)
	}
	
	
	def getImdbIdFromWebpages(topicEquivalentWebpages: List[String]): Option[String] = {
		
		val imdbEquivalents = topicEquivalentWebpages.filter(page => page.contains("imdb"))
		if (imdbEquivalents.size > 2){
			fileLog.warn(s"$freebaseId has more than one imdb")
			None
		}else if (imdbEquivalents.size == 1){
			val imdbIds = imdbEquivalents.head.split("/").filter(urlPart => urlPart.startsWith("tt"))
			val id = idFromIdmId(imdbIds.head)
			val msg = s"freebase: $freebaseId imdb: ${id}"
			//log.info(msg)
			//fileLog.info(msg)
			Some(id)
			
		} else{
			//log.info(s"no imdb for $freebaseId")
			None
		}
	}
	
	def idFromFreebaseId(): String = {
		s"m_${freebaseId.split("/").last}"
	}
	
	def idFromIdmId(id:String): String = {
		id.substring(2, id.length())
	}

}
