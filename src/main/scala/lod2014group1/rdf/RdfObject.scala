package lod2014group1.rdf

import java.text.DecimalFormat

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class RdfObject { }
abstract class RdfLiteral extends RdfObject {}

case class RdfInteger(lit: Integer) extends RdfLiteral {
	if (lit == null)
		throw new RuntimeException("lit is null.")
	override def toString: String = {
		'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#integer>"
	}
}

case class RdfUrl(lit: String) extends RdfLiteral {
	override def toString: String = {
		"<" + lit + ">"
	}
}

case class RdfDate(lit: DateTime) extends RdfLiteral {
	override def toString: String = {
		val format = DateTimeFormat.forPattern("yyyy-MM-dd")
		val formatString = format.print(lit)
		'"' + formatString + '"' + "^^<http://www.w3.org/2001/XMLSchema#date>"
	}
}
case class RdfString(lit: String) extends RdfLiteral {
	override def toString: String = {
		""" """" + lit.replace("\"", "\\\"") + '"'

	}
}

case class RdfBoolean(lit: Boolean) extends RdfLiteral {
	override def toString: String = {
		'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#boolean>"
	}

}

	case class RdfDouble(lit: Double) extends RdfLiteral {
		override def toString: String = {
			'"' + lit.toString + '"' + "^^<http://www.w3.org/2001/XMLSchema#double>"
		}

}
