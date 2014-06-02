package lod2014group1.triplification

import lod2014group1.rdf.RdfTriple
import net.liftweb.json.JsonAST.JValue
import lod2014group1.rdf.RdfMovieResource
import lod2014group1.rdf.RdfTriple
import lod2014group1.rdf.RdfTriple

object FreebaseExtraction {
	

	
}

class FreebaseExtraction() {
		
	def extractListString(json: JValue, values: Map[List[String], String => RdfTriple]): List[RdfTriple] = {
		
		implicit val formats = net.liftweb.json.DefaultFormats
		var triples = List[RdfTriple]()
		
		values.foreach( property => {
			val jsonValue = property._1.foldLeft(json)((acc, prop) => acc \ prop)
			val props = jsonValue.extractOpt[List[String]]
			props match {
				case Some(props) => props.foreach( prop => triples = property._2(prop) :: triples)
				case None => {
					val prop = jsonValue.extract[String]
					triples = property._2(prop) :: triples
				}
			}
			
		})

		triples
	}
	
}