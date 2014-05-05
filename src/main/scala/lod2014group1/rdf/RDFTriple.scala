package lod2014group1.rdf

case class RDFTriple(s: RDFResource, p: RDFResource, o: RDFObject) {

	override def toString(): String = {
		s"$s $p $o"
	}
}
case class RDFTuple(s: RDFResource, p: RDFResource) {
	def buildTriple(obj: RDFObject): RDFTriple = {
		RDFTriple(this.s, this.p, obj);
	}
}
