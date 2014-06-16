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

object FreebaseExtraction {
	

	
}

case class Person (text:String, id:String)
case class Persons (obj: List[Person])
//case class Crew (crew: List)

class FreebaseExtraction() {
		
	def extractListString(json: JValue, values: Map[List[String], String => RdfTriple]): List[RdfTriple] = {
		
		implicit val formats = net.liftweb.json.DefaultFormats
		//var triples = List[RdfTriple]()
		
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

	def extractPersons(json: JValue, values: Map[List[String],RdfPersonResource => RdfTriple]): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		values.flatMap(property => {
			val test2 = json.values
			val jsonValue = property._1.foldLeft(List(json)) { (acc, prop) =>
				acc.flatMap { jfield =>
					val obj = jfield \ prop
					if (jfield.isInstanceOf[JObject] && obj.isInstanceOf[net.liftweb.json.JsonAST$JNothing$]){
						println("jfield: " + jfield)
						println("prp " + prop)
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
				val (resource, triple) = matchPersons(person)
				property._2(resource) :: triple
			}
		}).toList
	}
	
	
	def matchPersons(p: Person):(RdfPersonResource, List[RdfTriple]) = {
		//TODO: match Persons and find right resource
		(new RdfPersonResource("www.freebase.com" + p.id), List())
	}
	
	def extractReleaseInfo(json: JValue, movieUri: String): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		val root = List("property", "/film/film/release_date_s", "values")
		val releases = root.foldLeft(json){ (acc, prop) =>	acc \ prop}
		val releaseList = if (releases.isInstanceOf[JArray])
							releases.asInstanceOf[JArray].arr
							else List(releases)
		

		val triple: List[RdfTriple] = releaseList.flatMap{info => 
			val obj = info \"property"
			val id = info \ "id"
			val releaseResource = RdfReleaseInfoResource.releaseInfoResourceFromRdfResource(RdfResource(s"${movieUri}/ReleaseInfo${id.extract[String]}"))
			val properties = Map[String, String => RdfTriple](
					//TODO ("/film/film_regional_release_date/film_release_distribution_medium", releaseResource.atDate(_:String)), 
				("/film/film_regional_release_date/film_release_region", releaseResource.country(_:String)),
				("/film/film_regional_release_date/release_date", releaseResource.atDate(_:String))
				)
			properties.map{prop => 
				val value = obj \ prop._1 \"values" \ "text"
				val valueObject = value.extract[String]
				prop._2(valueObject)
			}
								
		}
		println(triple)
		triple
	}
	
}