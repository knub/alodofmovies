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
	
}