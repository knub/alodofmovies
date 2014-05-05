package lod2014group1.rdf

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

abstract class RDFLiteral extends RDFObject {}

case class RDFInteger(lit: Integer) extends RDFLiteral
case class RDFURL(lit: Integer) extends RDFLiteral
case class RDFDate(lit: DateTime) extends RDFLiteral {
	override def toString(): String = {
		val format = DateTimeFormat.forPattern("y-M-d")
		val formatString = format.print(lit)
		'"' + formatString + '"' + "^^<http://www.w3.org/2001/XMLSchema#date>"
	}
}
