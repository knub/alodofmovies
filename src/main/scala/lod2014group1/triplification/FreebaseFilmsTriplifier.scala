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
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfPersonResource
import lod2014group1.rdf.RdfTriple

class FreebaseFilmsTriplifier(val freebaseId: String) extends Logging {
	
	val FREEBASE_URI = "www.freebase.com"

	protected val fileLog: Logger = LoggerFactory.getLogger("FreebaseFileLogger")
	
	def triplify(f: File): List[RdfTriple]= {
		
		
		val (id, triples) = getId(f)
		extractProperties(f, id)
	}
	
	def extractProperties(f: File, id: String): List[RdfTriple]={
		val json = JsonParser.parse(new FileReader(f))
		val r = RdfMovieResource.movieResourceFromRdfResource(RdfMovieResource.fromImdbId(id))
				
		val mapJsonToProperty = Map[List[String], String => RdfTriple](
		//reduce list of strings
				(List("property", "/common/topic/topic_equivalent_webpage", "values", "value") , r.sameAs(_: String)),
				(List("property", "/common/topic/article", "values", "property", "/common/document/text" ,"values", "value"), r.hasShortSummary(_: String)),
				(List("property", "/film/film/initial_release_date", "values", "value"), r.releasedInYear(_: String)),
				(List("property", "/type/object/name", "values", "value"), r.hasTitle(_: String)),
				(List("property", "/film/film/language", "values", "id"), r.shotInLanguage(_: String)),
				(List("property", "/film/film/language", "values", "text"), r.shotInLanguage(_: String)),
		//		(List("property", "/film/film/metacritic_id", "values", "text"), r.shotInLanguage(_: String)),
				//(List("property", "/common/topic/alias", "values", "value"), r.alsoKnownAs(_: RdfResource)),
				(List("property", "/type/object/key", "values", "value"), r.hasKeyword(_: String))
				//(List("property", "/common/topic/notable_for", "values", "text"), r.hasStoryLine(_: String))
						
				
				
				///common/topic/image
				)
		val extract = new FreebaseExtraction
		
		var triples = List[RdfTriple]()
		
		//triples = extract.extractListString(json, mapJsonToProperty)
		//if (triples.isEmpty){println (id)}
		
		val mapPersons = Map[List[String], RdfPersonResource => RdfTriple](
				(List("property", "/film/film/film_art_direction_by", "values") , r.artDirector(_: RdfResource)),
				(List("property", "/film/film/film_production_design_by", "values") , r.productionDesignBy(_: RdfResource)),
				(List("property", "/film/film/film_set_decoration_by", "values") , r.setDesignedBy(_: RdfResource)),
				(List("property", "/film/film/music", "values") , r.musicBy(_: RdfResource)),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/crewmember", "values") , r.hasOtherCrew(_: RdfResource)),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/film_crew_role", "values") , r.hasOtherCrew(_: RdfResource)),
				(List("property", "/film/film/film_casting_director", "values") , r.castingBy(_: RdfResource))
				)
				
		triples = extract.extractPersons(json, mapPersons) ::: triples
		println(triples)
				
		List()
	}
	
	def extractFromJson(json: JValue, values: Map[List[String], String => RdfTriple]): List[RdfTriple] = {
		
		
		List()
	}
	
	def getId(f: File): (String,List[RdfTriple])= {
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
			
			if (imdbId != "") {
							triples = RdfMovieResource.fromImdbId(imdbId).sameAs(FREEBASE_URI + freebaseId) :: triples// 
							id = imdbId
				} else {
				id = idFromFreebaseId()
			}
			
			getWikipediaFromWebpages(topicEquivalentWebpages)
			
			triples
			(id,triples)
		} catch{
			case e:net.liftweb.json.JsonParser$ParseException => {
				//fileLog.info(s"ParseException:$freebaseId")
				("",List())
			}
		}
		
	}
	
	
	def getImdbIdFromImdbTag(json: JValue): Option[String] = {
		implicit val formats = net.liftweb.json.DefaultFormats
			val rawImdb = json\\"/imdb/topic/title_id"\"values"\"value"
			try{
				val imdb = rawImdb.extract[String]
				//fileLog.info(s"imdb freebase:$freebaseId")
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
//			fileLog.warn(s"multiple mappings for freebase:$freebaseId")
			None
		}else if (imdbEquivalents.size == 1){
			val imdbIds = imdbEquivalents.head.split("/").filter(urlPart => urlPart.startsWith("tt"))
			if (!imdbIds.isEmpty){
				val id = idFromIdmId(imdbIds.head)
				val msg = s"equivalent freebase:$freebaseId"
				//log.info(msg)
//				fileLog.info(msg)
				Some(id)
			} else {
//				fileLog.info(s"wrong id given:${imdbEquivalents.head} freebase:$freebaseId")
				None
			}
		} else{
//			fileLog.info(s"freebase:$freebaseId")
			None
		}
	}
	
	def getWikipediaFromWebpages(topicEquivalentWebpages: List[String]): List[String] = {
		
		val wikiEquivalents = topicEquivalentWebpages.filter(page => page.contains("wikipedia"))
		//if (wikiEquivalents.size > 0) {fileLog.info("wiki equivalent")}
		wikiEquivalents
	}
	
	
	def idFromFreebaseId(): String = {
		s"m_${freebaseId.split("/").last}"
	}
	
	def idFromIdmId(id:String): String = {
		id.substring(2, id.length())
	}

}
