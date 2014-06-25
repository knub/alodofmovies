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
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.UriBuilder
import com.hp.hpl.jena.vocabulary.RDFTest
import lod2014group1.rdf.RdfTriple
import org.apache.commons.io.FileUtils

class FreebaseFilmsTriplifier() extends Triplifier with Logging {

	protected val fileLog: Logger = LoggerFactory.getLogger("FreebaseFileLogger")
	
	def triplify(file: File): List[RdfTriple] = {
		val freebaseId = s"/m/${file.getName()}"
		triplify(FileUtils.readFileToString(file))
	}
	
	def triplify(content: String): List[RdfTriple]= {
		val (id, movieResource, triples) = getId(content)
		
		val proptriples = (movieResource, id) match{
			case (Some(movieResource), Some(id)) => extractProperties(content, id, movieResource)
			case _ => {log.info(s"failed by id parsing ${id}")
				List()
			}
		}
		triples:::proptriples
	}
	
	def extractProperties(content: String, id: String, movieResource: RdfMovieResource): List[RdfTriple]={
		val json = JsonParser.parse(content)
				
		val mapJsonToProperty = Map[List[String], String => RdfTriple](
				(List("property", "/common/topic/topic_equivalent_webpage", "values", "value") , movieResource.sameAs(_: String)),
				(List("property", "/common/topic/article", "values", "property", "/common/document/text" ,"values", "value"), movieResource.hasShortSummary(_: String)),
				(List("property", "/film/film/initial_release_date", "values", "value"), movieResource.releasedInYear(_: String)),
				(List("property", "/type/object/name", "values", "value"), movieResource.hasTitle(_: String)),
				(List("property", "/type/object/name", "values", "value"), movieResource.hasLabel(_: String)),
				(List("property", "/film/film/language", "values", "id"), movieResource.shotInLanguage(_: String)),
				(List("property", "/film/film/language", "values", "text"), movieResource.shotInLanguage(_: String)),
				(List("property", "/film/film/tagline", "values", "value"), movieResource.hasTagline(_: String)),
				(List("property", "/film/film/netflix_id", "values", "value"), movieResource.hasNetflixId(_: String)), 
				(List("property", "/film/film/nytimes_id", "values", "value"), movieResource.hasNytimesId(_: String)),
				(List("property", "/film/film/apple_movietrailer_id", "values", "value"), movieResource.hasAppleMovietrailerId(_: String)), 
				(List("property", "/film/film/rottentomatoes_id", "values", "value"), movieResource.hasRottentomatoesId(_: String)), 
				(List("property", "/film/film/traileraddict_id", "values", "value"), movieResource.hasTraileraddictId(_: String)), 
				(List("property", "/film/film/fandango_id", "values", "value"), movieResource.hasFandangoId(_: String)), 
				(List("property", "/film/film/metacritic_id", "values", "text"), movieResource.hasMetacriticId(_: String)),
				(List("property", "/film/film/film_subject", "values", "text"), movieResource.hasSubject(_: String)),
				(List("property", "/common/topic/alias", "values", "value"), movieResource.hasAlternativeName(_:String)),
				(List("property", "/type/object/key", "values", "value"), movieResource.hasKeyword(_: String))
		)
		
		val extract = new FreebaseExtraction
		
		var triples = extract.extractListString(json, mapJsonToProperty)

		//TODO
		// awards
		// /film/film/rating - releaseInfo, getcountry
		// /film/film/runtime --> more than 1 per movie, (cut versions, countries...) 
		// /film/film/soundtrack
		// /film/film/subjects
		// /film/film/trailers --> video?
		// /media_common/netflix_title/netflix_genres
		// /media_common/quotation_source/quotations
		// /type/object/key
		// /type/object/name
		// /film/film/personal_appearances
		
		val mapPersons = Map[List[String], (RdfPersonResource => RdfTriple, Option[RdfResource], Option[String])](
				(List("property", "/film/film/film_art_direction_by", "values") , (movieResource.artDirector(_: RdfResource), Some(RdfPersonResource.director), Some("Art Director"))),
				(List("property", "/film/film/film_production_design_by", "values") , (movieResource.productionDesignBy(_: RdfResource), None, Some("Production Desginer"))),
				(List("property", "/film/film/film_set_decoration_by", "values") , (movieResource.setDecoratedBy(_: RdfResource), Some(RdfPersonResource.setDesigner), Some("Set Decorater"))),
				(List("property", "/film/film/music", "values") , (movieResource.musicBy(_: RdfResource), None, Some("Composer"))),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/crewmember", "values") , (movieResource.hasOtherCrew(_: RdfResource), None, None)),
				(List("property", "/film/film/other_crew", "values", "property", "/film/film_crew_gig/film_crew_role", "values") , (movieResource.hasOtherCrew(_: RdfResource), None, None)),
				(List("property", "/film/film/produced_by", "values") , (movieResource.producedBy(_: RdfResource), Some(RdfPersonResource.producer), Some("Producer"))),
				(List("property", "/film/film/story_by", "values") , (movieResource.storyBy(_: RdfResource), Some(RdfPersonResource.storyEditor), Some("Story Writer"))),
				(List("property", "/film/film/written_by", "values") , (movieResource.writtenBy(_: RdfResource), None, Some("Writer"))),
				(List("property", "/film/film/film_casting_director", "values") , (movieResource.castingBy(_: RdfResource), None, Some("Casting Director")))
		)
				
		triples = extract.extractPersons(json, mapPersons) ::: triples
		
		val compoundList = Map[List[String], (String, JValue) => (Map[List[String], String => RdfTriple], List[RdfTriple])](
				 (List("property", "/film/film/release_date_s", "values"), this.releaseInfoProps(_:String, _:JValue)),
				 (List("property", "/award/award_nominated_work/award_nominations", "values"), this.awardsNominationProps(_:String, _:JValue)),
				 (List("property", "/award/award_winning_work/awards_won", "values"), this.awardsWonProps(_:String, _:JValue))
		)
		
		val compounds = extract.extractCompounds(json, id, compoundList)
		triples = compounds ::: triples
		triples = extract.extractStarring(json, movieResource, id) ::: triples
		
		val sequences = Map[List[String], (Person, RdfMovieResource) => List[RdfTriple]](
				(List("property", "/film/film/sequel", "values"), sequels(_:Person, _:RdfMovieResource)),
				(List("property", "/film/film/prequel", "values"), prequels(_:Person, _:RdfMovieResource))
		)
			
		triples ::: extract.extractResources(json, sequences, movieResource)		
	}
	
	def sequels (p:Person, movieResource: RdfMovieResource): List[RdfTriple] = {
		val (movie, triple) = defineMovie(p)
		movieResource.nextMovie(movie) :: triple 
	}
	
	def prequels (p:Person, movieResource: RdfMovieResource): List[RdfTriple] = {
		val (movie, triple) = defineMovie(p)
		movieResource.previousMovie(movie) :: triple 
	}
	
	def defineMovie(p: Person): (RdfMovieResource, List[RdfTriple]) = {
		val movie = new RdfMovieResource(UriBuilder.getFreebaseUri(p.id))
		val triple = List(
			movie.isA(RdfMovieResource.film),	
			movie.hasName(p.text),
			movie.hasLabel(p.text)
		)
		(movie, triple)
	}
	
	def releaseInfoProps(movieUri:String, id:JValue): (Map[List[String], String => RdfTriple], List[RdfTriple]) ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val idvalue = id.extractOpt[String]
		idvalue match {
			case Some(idvalue) => {
				val releaseResource = new RdfReleaseInfoResource(UriBuilder.getReleaseInfoUriFromFreebaseId(idvalue))
				val properties = Map[List[String], String => RdfTriple](
						(List("/film/film_regional_release_date/film_release_distribution_medium", "values","text"), releaseResource.medium(_:String)), 
						(List("/film/film_regional_release_date/film_release_region", "values", "text"), releaseResource.country(_:String)),
						(List("/film/film_regional_release_date/release_date", "values", "text"), releaseResource.atDate(_:String))
						)
				(properties, List(releaseResource isA RdfReleaseInfoResource.releaseInfo, releaseResource sameAs(UriBuilder.getFreebaseUri(idvalue))))
			}
			case None => (Map[List[String], String => RdfTriple](), List())
			
		}
	}

	def awardsNominationProps(movieUri:String, id:JValue): (Map[List[String], String => RdfTriple], List[RdfTriple]) ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val idvalue = id.extractOpt[String]
		idvalue match {
			case Some(idvalue) => {
				val awardResource = new RdfAwardResource(UriBuilder.getAwardUriFromFreebaseId(idvalue))
				val properties = Map[List[String], String => RdfTriple](
						(List("/award/award_nomination/award", "values", "text"), awardResource.inCategory(_:String)),
						(List("/award/award_nomination/award_nominee", "values", "id"), awardResource.forFreebaseNominee(_:String)),
						(List("/award/award_nomination/ceremony", "values", "text"), awardResource.hasName(_:String)),
						(List("/award/award_nomination/year", "values", "text"), awardResource.inYear(_:String))
						)
				(properties, List(awardResource isA RdfAwardResource.award, awardResource.hasOutcome("Nominated"), awardResource sameAs(UriBuilder.getFreebaseUri(idvalue))))
			}
			case None => (Map[List[String], String => RdfTriple](), List())
		}
	}
	
	def awardsWonProps(movieUri:String, id:JValue): (Map[List[String], String => RdfTriple], List[RdfTriple]) ={
		implicit val formats = net.liftweb.json.DefaultFormats
		val idvalue = id.extractOpt[String]
		idvalue match {
			case Some(idvalue) => {		
				val awardResource = new RdfAwardResource(UriBuilder.getAwardUriFromFreebaseId(idvalue))
				val properties = Map[List[String], String => RdfTriple](
						(List("/award/award_honor/award", "values", "text"), awardResource.inCategory(_:String)),
						(List("/award/award_honor/award_winner", "values", "id"), awardResource.forFreebaseNominee(_:String)),
						(List("/award/award_honor/ceremony", "values", "text"), awardResource.hasName(_:String)),
						(List("/award/award_honor/year", "values", "text"), awardResource.inYear(_:String))
					)
				(properties, List(awardResource isA RdfAwardResource.award, awardResource.hasOutcome("Honor"), awardResource sameAs(UriBuilder.getFreebaseUri(idvalue))))
			}
			case None => (Map[List[String], String => RdfTriple](), List())
		}
	}

	def getId(content: String): (Option[String], Option[RdfMovieResource], List[RdfTriple])= {
		
		implicit val formats = net.liftweb.json.DefaultFormats
		try{
			val json = JsonParser.parse(content)
			
			val freebaseId = (json\\"/type/object/mid"\"values"\"value").extract[String]
			
			val tEWebpageJson = json\\"/common/topic/topic_equivalent_webpage"\"values"\"value"
			var webpages = List[String]()
			if (tEWebpageJson.isInstanceOf[net.liftweb.json.JsonAST.JString])
				webpages = List[String](tEWebpageJson.extract[String])
			else webpages = tEWebpageJson.extract[List[String]]
			
			val imdbId = getImdbIdFromImdbTag(json).getOrElse(getImdbIdFromWebpages(webpages).getOrElse(""))
			
			val (id, movieUri, movieResource, samAsTriples) = if (imdbId != "") {	
				val uri = UriBuilder.getMovieUriFromImdbId(imdbId)
				val movie = new RdfMovieResource(uri)
				(imdbId, uri, movie, List(movie sameAsImdbUrl(imdbId)))
			} else {	
				val uri = UriBuilder.getMovieUriFromImdbId(freebaseId)
				val movie = new RdfMovieResource(uri)
				(freebaseId, uri, movie , List())
			}
			
			val movie = new RdfMovieResource(movieUri)
			val triples = List( movie sameAs(UriBuilder.getFreebaseUri(freebaseId)),
					movie isA RdfMovieResource.film
					) ::: samAsTriples
			(Some(id),Some(movie),triples)
		} catch{
			case e:net.liftweb.json.JsonParser$ParseException => {
				(None, None, List())
			}
		}
		
	}
	
	def getImdbIdFromImdbTag(json: JValue): Option[String] = {
		implicit val formats = net.liftweb.json.DefaultFormats
			val rawImdb = json\\"/imdb/topic/title_id"\"values"\"value"
			try{
				val imdb = rawImdb.extract[String]
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
			None
		}else if (imdbEquivalents.size == 1){
			val imdbIds = imdbEquivalents.head.split("/").filter(urlPart => urlPart.startsWith("tt"))
			if (!imdbIds.isEmpty){
				val id = imdbIds.head
				Some(id)
			} else {
				None
			}
		} else{
			None
		}
	}
}
