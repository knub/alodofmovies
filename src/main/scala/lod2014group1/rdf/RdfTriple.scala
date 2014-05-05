package lod2014group1.rdf

case class RdfTriple(s: RdfResource, p: RdfResource, o: RdfObject) {

	override def toString(): String = {
		s"$s $p $o"
	}
}
case class RDFTuple(s: RdfResource, p: RdfResource) {
	def buildTriple(obj: RdfObject): RdfTriple = {
		RdfTriple(this.s, this.p, obj);
	}
}
