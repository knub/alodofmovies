package lod2014group1.rdf

case class RdfResource(val uri: String) extends RdfObject {
	def buildTriple(predicate: RdfResource, obj: RdfObject): RdfTriple = {
		RdfTriple(this, predicate, obj);
	}

	override def toString(): String = {
		if (uri startsWith "http")
			"<" + uri + ">"
		else
			uri
	}

	def isA(entity: RdfResource): RdfTriple = {
		this.buildTriple(RdfResource("rdf:type"), entity)
	}

	def isAn = isA _

	def sameAs(url: String): RdfTriple = {
		this.buildTriple(RdfResource("owl:sameAs"), RdfUrl(url))
	}
}

case class RdfTriple(s: RdfResource, p: RdfResource, o: RdfObject) {
	override def toString(): String = {
		s"$s $p $o ."
	}
}
