package lod2014group1.rdf

case class RdfResource(val uri: String) extends RdfObject {
	def buildTriple(predicate: RdfResource, obj: RdfObject): RdfTriple = {
		RdfTriple(this, predicate, obj);
	}

	override def toString(): String = {
		if (uri startsWith "http")
			"<" + uri + ">"
		uri
	}
}

case class RdfTriple(s: RdfResource, p: RdfResource, o: RdfObject) {
	override def toString(): String = {
		s"$s $p $o"
	}
}
