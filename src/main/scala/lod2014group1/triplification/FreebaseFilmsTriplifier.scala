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
import net.liftweb.json.JsonAST.JValue

class FreebaseFilmsTriplifier(val freebaseId: String) extends Logging {
	
	val FREEBASE_URI = "www.freebase.com"

	protected val fileLog: Logger = LoggerFactory.getLogger("FreebaseFileLogger")
	
	def triplify(f: File): List[RdfTriple]= {
		
		var triples: List[RdfTriple] = List()
		
		implicit val formats = net.liftweb.json.DefaultFormats
		try{
			val json = JsonParser.parse(new FileReader(f))
			
			val topicEquivalentWebpageJson = json\\"/common/topic/topic_equivalent_webpage"\"values"\"value"
			var topicEquivalentWebpages = List[String]()
			if (topicEquivalentWebpageJson.isInstanceOf[net.liftweb.json.JsonAST.JString])
				topicEquivalentWebpages = List[String](topicEquivalentWebpageJson.extract[String])
			else topicEquivalentWebpages = topicEquivalentWebpageJson.extract[List[String]]
			
			var imdbId = getImdbIdFromImdbTag(json).getOrElse(getImdbIdFromWebpages(topicEquivalentWebpages).getOrElse(""))
			
			var id = ""
			
			if (imdbId != ""){
						triples = RdfMovieResource.fromImdbId(imdbId).sameAs(FREEBASE_URI + freebaseId) :: triples// 
						id = imdbId
			} else {
				id = idFromFreebaseId()
			}
			
			triples
		} catch{
			case e:net.liftweb.json.JsonParser$ParseException => {
				fileLog.info(s"ParseException:$freebaseId")
				List()
			}
		}
	}
	
	
	def getImdbIdFromImdbTag(json: JValue): Option[String] = {
		implicit val formats = net.liftweb.json.DefaultFormats
			val rawImdb = json\\"/imdb/topic/title_id"\"values"\"value"
			try{
				val imdb = rawImdb.extract[String]
				fileLog.info(s"imdb freebase:$freebaseId")
				Some (imdb)	
			}
			catch {
				case me: net.liftweb.json.MappingException => {
					None
				}
			}
	}
	
	
	def getImdbIdFromWebpages(topicEquivalentWebpages: List[String]): Option[String] = {
		
		val imdbEquivalents = topicEquivalentWebpages.filter(page => page.contains("imdb"))
		if (imdbEquivalents.size > 2){
			fileLog.warn(s"multiple mappings for freebase:$freebaseId")
			None
		}else if (imdbEquivalents.size == 1){
			val imdbIds = imdbEquivalents.head.split("/").filter(urlPart => urlPart.startsWith("tt"))
			if (!imdbIds.isEmpty){
				val id = idFromIdmId(imdbIds.head)
				val msg = s"equivalent freebase:$freebaseId"
				//log.info(msg)
				fileLog.info(msg)
				Some(id)
			} else {
				fileLog.info(s"wrong id given:${imdbEquivalents.head} freebase:$freebaseId")
				None
			}
		} else{
			fileLog.info(s"freebase:$freebaseId")
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
