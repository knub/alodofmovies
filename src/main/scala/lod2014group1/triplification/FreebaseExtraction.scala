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
	
	def extractCompounds(json: JValue, movieUri:String, properties: Map[List[String], (String, JValue) => Map[List[String], String => RdfTriple]]): List[RdfTriple] = {
		implicit val formats = net.liftweb.json.DefaultFormats
		
		
		val t: List[RdfTriple] = properties.flatMap {value => 
			val compounds = value._1.foldLeft(json){ (acc, prop) =>	acc \ prop}
			val compoundList = if (compounds.isInstanceOf[JArray])
								compounds.asInstanceOf[JArray].arr
								else List(compounds)
	
			compoundList.flatMap{info => 
				val obj = info \"property"
				val id = info \ "id"
				value._2(movieUri, id).flatMap { prop => 
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
				}						
			}
		}.toList
		println(t)
		t
	}
	

	
}