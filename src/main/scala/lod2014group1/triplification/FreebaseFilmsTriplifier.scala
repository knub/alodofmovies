package lod2014group1.triplification

import java.io.File
import java.io.FileReader
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.slf4s.Logging
import org.slf4s.Logger
import org.slf4s.LoggerFactory
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfMovieResource
import lod2014group1.rdf.RdfPersonResource
import lod2014group1.rdf.RdfReleaseInfoResource
import lod2014group1.rdf.RdfAwardResource
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfResource

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
				(List("property", "/film/film/tagline", "values", "value"), r.hasTagline(_: String)),
				(List("property", "/imdb/topic/title_id", "values", "value"), r.sameAs(_: String)), // --> id not resource
		//		(List("property", "/film/film/metacritic_id", "values", "text"), r.shotInLanguage(_: String)),
				//(List("property", "/common/topic/alias", "values", "value"), r.alsoKnownAs(_: RdfResource)),
				(List("property", "/type/object/key", "values", "value"), r.hasKeyword(_: String))
				//(List("property", "/common/topic/notable_for", "values", "text"), r.hasStoryLine(_: String))
						
				
				
				///common/topic/image//	
				//(List("property", "/film/film/production_companies", "values") , r.xxx(_: RdfResource)),

				
				)
		val extract = new FreebaseExtraction
		
		var triples = List[RdfTriple]()
		
		//triples = extract.extractListString(json, mapJsonToProperty)
		//if (triples.isEmpty){println (id)}
		
		//TODO
		// awards
		// /film/film/rating - releaseInfo, getcountry
		// /film/film/release_date_s
		// /film/film/rottentomatoes_id
		// /film/film/runtime --> more than 1 per movie, (cut versions, countries...) 
		// /film/film/metacritic_id
		// /film/film/soundtrack
		// /film/film/starring
		// /film/film/subjects
		// /film/film/traileraddict_id --not important
		// /film/film/trailers --> video?
		// /media_common/netflix_title/netflix_genres
		// /media_common/quotation_source/quotations
		// /type/object/key
		// /type/object/name
		// /film/film/personal_appearances
		
		val mapPersons = Map[List[String], RdfPersonResource => RdfTriple](
				(List("property", "/film/film/film_art_direction_by", "values") , r.artDirector(_: RdfResource)),
				(List("property", "/film/film/film_production_design_by", "values") , r.productionDesignBy(_: RdfResource)),
				(List("property", "/film/film/film_set_decoration_by", "values") , r.setDecoratedBy(_: RdfResource)),
				(List("property", "/film/film/music", "values") , r.musicBy(_: RdfResource)),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/crewmember", "values") , r.belongsToOtherCrew(_: RdfResource)),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/film_crew_role", "values") , r.belongsToOtherCrew(_: RdfResource)),
				(List("property", "/film/film/produced_by", "values") , r.producedBy(_: RdfResource)),
				(List("property", "/film/film/story_by", "values") , r.storyBy(_: RdfResource)),
				(List("property", "/film/film/written_by", "values") , r.writtenBy(_: RdfResource)),
			//	(List("property", "/film/film/personal_appearances", "values", "property", "/film/personal_film_appearance/person", "values") , r.personal_appearnce(_: RdfResource)),
				(List("property", "/film/film/film_casting_director", "values") , r.castingBy(_: RdfResource))
				)
				
		triples = extract.extractPersons(json, mapPersons) ::: triples
		println(triples)
		val release = Map[List[String], (String, JValue) => Map[List[String], String => RdfTriple]](
				 (List("property", "/film/film/release_date_s", "values"), this.releaseInfoProps(_:String, _:JValue)),
				 (List("property", "/award/award_nominated_work/award_nominations", "values"), this.awardsNominationProps(_:String, _:JValue)),
				 (List("property", "/award/award_winning_work/awards_won", "values"), this.awardsNominationProps(_:String, _:JValue))
				 
		)
		
		val releaseInfo = extract.extractCompounds(json, id, release)
		println(releaseInfo)
		triples = releaseInfo ::: triples
		List()
	}
	
	def releaseInfoProps(movieUri:String, id:JValue): Map[List[String], String => RdfTriple] ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val releaseResource = RdfReleaseInfoResource.releaseInfoResourceFromRdfResource(RdfResource(s"${movieUri}/ReleaseInfo${id.extract[String]}"))
		val properties = Map[List[String], String => RdfTriple](
				//TODO ("/film/film_regional_release_date/film_release_distribution_medium", releaseResource.atDate(_:String)), 
				(List("/film/film_regional_release_date/film_release_region", "values", "text"), releaseResource.country(_:String)),
				(List("/film/film_regional_release_date/release_date", "values", "text"), releaseResource.atDate(_:String))
			)
		properties
	}

	def awardsNominationProps(movieUri:String, id:JValue): Map[List[String], String => RdfTriple] ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val awardResource = RdfAwardResource.awardResourceFromRdfResource(RdfResource (s"${movieUri}/NAwards${id.extract[String]}"))
		awardResource.hasOutcome("Nominated")
		val properties = Map[List[String], String => RdfTriple](
				(List("/award/award_nomination/award", "values", "text"), awardResource.inCategory(_:String)),
				(List("/award/award_nomination/award_nominee", "values", "id"), awardResource.forFreebaseNominee(_:String)),
				(List("/award/award_nomination/ceremony", "values", "text"), awardResource.hasName(_:String)),
				(List("/award/award_nomination/year", "values", "text"), awardResource.inYear(_:String))
			)
		properties
	}
	
	def awardsWonProps(movieUri:String, id:JValue): Map[List[String], String => RdfTriple] ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val awardResource = RdfAwardResource.awardResourceFromRdfResource(RdfResource (s"${movieUri}/WAwards${id.extract[String]}"))
		awardResource.hasOutcome("Honor")
		val properties = Map[List[String], String => RdfTriple](
				(List("/award/award_honor/award", "values", "text"), awardResource.inCategory(_:String)),
				(List("/award/award_honor/award_winner", "values", "id"), awardResource.forFreebaseNominee(_:String)),
				(List("/award/award_honor/ceremony", "values", "text"), awardResource.hasName(_:String)),
				(List("/award/award_honor/year", "values", "text"), awardResource.inYear(_:String))
			)
		properties
	}
	
	def starring(movieUri:String, id:JValue): Map[List[String], String => RdfTriple] ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val awardResource = RdfAwardResource.awardResourceFromRdfResource(RdfResource (s"${movieUri}/WAwards${id.extract[String]}"))
		awardResource.hasOutcome("Honor")
		val properties = Map[List[String], String => RdfTriple](
				(List("/award/award_honor/award", "values", "text"), awardResource.inCategory(_:String)),
				(List("/award/award_honor/award_winner", "values", "id"), awardResource.forFreebaseNominee(_:String)),
				(List("/award/award_honor/ceremony", "values", "text"), awardResource.hasName(_:String)),
				(List("/award/award_honor/year", "values", "text"), awardResource.inYear(_:String))
			)
		properties
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
