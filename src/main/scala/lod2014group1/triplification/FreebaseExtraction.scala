package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import net.liftweb.json.JsonAST.JValue
import lod2014group1.rdf.RdfMovieResource
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfPersonResource
import lod2014group1.rdf.RdfTriple
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JNothing
import lod2014group1.rdf.RdfReleaseInfoResource
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfCharacterResource
import lod2014group1.rdf.RdfAwardResource
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.RdfResource
import lod2014group1.rdf.UriBuilder

object FreebaseExtraction {
	

	
}

case class Person (text:String, id:String)

class FreebaseExtraction() {
		
	def extractListString(json: JValue, values: Map[List[String], String => RdfTriple]): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		values.flatMap( property => {
			val jsonValue = property._1.foldLeft(json)((acc, prop) => acc \ prop)
			val props = jsonValue.extractOpt[List[String]]
			props match {
				case Some(props) => props.map( prop => property._2(prop))
				case None => {
					val prop = jsonValue.extract[String]
					List(property._2(prop))
				}
			}
		}).toList

	}

	def extractPersons(json: JValue, values: Map[List[String],(RdfPersonResource => RdfTriple, Option[RdfResource], Option[String])]): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		values.flatMap(property => {
			val test2 = json.values
			val jsonValue = property._1.foldLeft(List(json)) { (acc, prop) =>
				acc.flatMap { jfield =>
					val obj = jfield \ prop
					if (jfield.isInstanceOf[JObject] && obj.isInstanceOf[net.liftweb.json.JsonAST$JNothing$]){
						//println("jfield: " + jfield)
						//println(s"no $prop")
						List()
					} else
					if (obj.isInstanceOf[JArray])
						obj.asInstanceOf[JArray].arr
					else
						List(obj)
				}
			}
			
			jsonValue.flatMap{value => 
				val person = value.extract[Person]
				val (resource, triple) = matchPersons(person, property._2._2, property._2._3)
				property._2._1(resource) :: triple
			}
		}).toList
	}
	
	
	def matchPersons(p: Person, resource: Option[RdfResource],job: Option[String]):(RdfPersonResource, List[RdfTriple]) = {
		val person = new RdfPersonResource(UriBuilder.getPersonUriFromFreebaseId(p.id))
		val triple = List(person isA RdfPersonResource.actor,
				person isA(RdfPersonResource.person),
				person hasLabel p.text)
		val resourceTriple = resource match {
			case Some(resource) => List(person isA resource)
			case None => List()
		}
		val jobTriple = job match {
			case Some(job) => List(person hasJob(job))
			case None => List()
		}
		(person, triple ::: resourceTriple ::: jobTriple)
	}
	
	def extractResources(json: JValue, values: Map[List[String], (Person, RdfMovieResource) => List[RdfTriple]], movie: RdfMovieResource): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		values.flatMap(property => {
			val test2 = json.values
			val jsonValue = property._1.foldLeft(List(json)) { (acc, prop) =>
				acc.flatMap { jfield =>
					val obj = jfield \ prop
					if (jfield.isInstanceOf[JObject] && obj.isInstanceOf[net.liftweb.json.JsonAST$JNothing$]){
						List()
					} else
					if (obj.isInstanceOf[JArray])
						obj.asInstanceOf[JArray].arr
					else
						List(obj)
				}
			}
			
			jsonValue.flatMap{value => 
				val person = value.extract[Person]
				property._2(person, movie)
			}.toList
		}).toList
	}
	
	
	
	
	def extractCompounds(json: JValue, movieUri:String, properties: Map[List[String], (String, JValue) => (Map[List[String], String => RdfTriple], List[RdfTriple])]): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		
		val t: List[RdfTriple] = properties.flatMap {value => 
			val compounds = value._1.foldLeft(json){ (acc, prop) =>	acc \ prop}
			val compoundList = if (compounds.isInstanceOf[JArray])
								compounds.asInstanceOf[JArray].arr
								else List(compounds)
	
			compoundList.flatMap{info => 
				val obj = info \"property"
				val id = info \ "id"
				val (funMap: Map[List[String], String => RdfTriple], triple: List[RdfTriple]) = value._2(movieUri, id)
				
				funMap.flatMap { prop => 
					val value =	prop._1.foldLeft(obj){ (acc, prop) =>	acc \ prop}
					if (!value.isInstanceOf[net.liftweb.json.JsonAST$JNothing$]){
						val valueList = if (value.isInstanceOf[JArray])
											value.asInstanceOf[JArray].arr
										else
											List(value)
						valueList.flatMap{v => 
							val valueObject = v.extract[String]
							List(prop._2(valueObject))}
					} else
						List()
				}.toList ::: triple
				
			}
		}.toList
		t
	}
	
	def extractStarring(json: JValue, movie: RdfMovieResource, movieid : String): List[RdfTriple]= {
		implicit val formats = net.liftweb.json.DefaultFormats
		val extract = List("property", "/film/film/starring", "values", "property") 
		val jsonValueList = extract.foldLeft(List(json)) { (acc, prop) =>
			acc.flatMap { jfield =>
				val obj = jfield \ prop
				if (jfield.isInstanceOf[JObject] && obj.isInstanceOf[net.liftweb.json.JsonAST$JNothing$]){
					List()
				} else
				if (obj.isInstanceOf[JArray])
					obj.asInstanceOf[JArray].arr
				else
					List(obj)
			}
		}
		
		jsonValueList.flatMap{starringJson => 
			val actorJson = starringJson \ "/film/performance/actor" \ "values"
			val person = actorJson.extractOpt[Person]
			
			person match {
				case Some(person) => {
					val personResource = new RdfPersonResource(UriBuilder.getPersonUriFromFreebaseId(person.id))
					
					val triple = List(personResource isA RdfPersonResource.actor, personResource.hasName(person.text), personResource.hasLabel(person.text))
					
					val characterJson = starringJson \ "/film/performance/character" \ "values"
					val character = characterJson.extractOpt[Person]
					
					val charactertriple = character match {
						case Some(character) => {
							val characterResource = new RdfCharacterResource(UriBuilder.getMovieCharacterUriFromFreebaseId(movieid, character.id))
							val chaResource = new RdfCharacterResource(UriBuilder.getCharacterUriFromFreebaseId(character.id))
							List(characterResource isA RdfCharacterResource.character,
								characterResource.hasName(character.text),
								characterResource hasLabel character.text,
								characterResource.inMovie(movie),
								characterResource.playedBy(personResource),
								chaResource isA(RdfCharacterResource.character),
								chaResource hasLabel character.text,
								characterResource.isSubclassOf(chaResource))
						}
						case None => {
							val specialPerformanceJson = starringJson \ "/film/performance/special_performance_type" \ "values"
							val specialPerformance = specialPerformanceJson.extractOpt[Person]
							specialPerformance match {
								case Some(specialPerformance) => {
									if (specialPerformance.text == "Him/Herself"){
							val characterResource = new RdfCharacterResource(UriBuilder.getMovieCharacterUriFromFreebaseId(movieid, person.id))
							val chaResource = new RdfCharacterResource(UriBuilder.getCharacterUriFromFreebaseId(person.id))
										List(characterResource isA RdfCharacterResource.character,
												characterResource.hasName(person.text),
												characterResource.inMovie(movie),
												characterResource hasLabel person.text,
												characterResource.playedBy(personResource),
												chaResource isA RdfCharacterResource.character,
												chaResource hasLabel person.text,
												characterResource.isSubclassOf(chaResource)
										)
									} else List()
								} 
								case None => List()
							}
						}

					}
					charactertriple ::: triple
				}
				
			case None => List()
			}
		}	
	}
	

	
}