package lod2014group1.rdf

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RdfObject { }
abstract class RdfLiteral extends RdfObject {}

case class RdfInteger(lit: Integer) extends RdfLiteral
case class RdfUrl(url: String) extends RdfLiteral
case class RdfDate(lit: DateTime) extends RdfLiteral {
	override def toString(): String = {
		val format = DateTimeFormat.forPattern("y-M-d")
		val formatString = format.print(lit)
		'"' + formatString + '"' + "^^<http://www.w3.org/2001/XMLSchema#date>"
	}
}
case class RdfString(str: String) extends RdfLiteral
