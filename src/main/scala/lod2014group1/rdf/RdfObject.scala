package lod2014group1.rdf

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RdfObject { }
abstract class RdfLiteral extends RdfObject {}

case class RdfInteger(lit: Integer) extends RdfLiteral {
	override def toString(): String = {
		'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#integer>"
	}
}

case class RdfUrl(lit: String) extends RdfLiteral {
	override def toString(): String = {
		"<" + lit + ">"
	}
}

case class RdfDate(lit: DateTime) extends RdfLiteral {
	override def toString(): String = {
		val format = DateTimeFormat.forPattern("y-M-d")
		val formatString = format.print(lit)
		'"' + formatString + '"' + "^^<http://www.w3.org/2001/XMLSchema#date>"
	}
}
case class RdfString(lit: String) extends RdfLiteral {
	override def toString(): String = {
		'"' + lit.replace("\"", "\\\"") + '"'
	}
	//	override def toString(): String = {
	//		'"' + lit + '"' + "^^<http://www.w3.org/2001/XMLSchema#string>"
	//	}
	//
	//	override def toString(): String = {
	//		'"' + lit + '"' + "@en"
	//	}
}

case class RdfBoolean(lit: Boolean) extends RdfLiteral {
	override def toString(): String = {
		'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#boolean>"
	}

}

	case class RdfDouble(lit: Double) extends RdfLiteral {
		override def toString(): String = {
			'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#double>"
		}

}
