package lod2014group1.rdf

import org.joda.time.DateTime

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

	def isA(entity: RdfResource): RdfTriple = this.buildTriple(RdfResource("rdf:type"), entity)
	def isAn = isA _

	def sameAs(url: String): RdfTriple = this.buildTriple(RdfResource("owl:sameAs"), RdfUrl(url))

	def name(name: String): RdfTriple = buildTriple(RdfResource("dbpprop:name"), RdfString(name))
	def hasName = name _

	def country(country: String): RdfTriple = buildTriple(RdfResource("dbpprop:country"), RdfString(country))
	def inCountry = country _

	def year(year: String): RdfTriple = buildTriple(RdfResource("dbpprop:years"), RdfString(year))
	def inYear = year _

	def description(description: String): RdfTriple = buildTriple(RdfResource("dbpprop:description"), RdfString(description))
	def hasDescription = description _

	def abstractContent(abstractStr: String): RdfTriple = buildTriple(RdfResource("dbpedia-owl:abstact"), RdfString(abstractStr))

	def dateRes(date: String): RdfTriple = buildTriple(RdfResource("dbpprop:date"), RdfString(date))

	def dateRes(date: DateTime): RdfTriple = buildTriple(RdfResource("dbpprop:date"), RdfDate(date))

	def subject(resource: RdfResource): RdfTriple = buildTriple(RdfResource("dcterms:subject"), resource)

	def hasAward(award: RdfResource): RdfTriple = buildTriple(RdfResource("lod:hasAward"), award)

}

case class RdfTriple(s: RdfResource, p: RdfResource, o: RdfObject) {
	override def toString(): String = {
		s"$s $p $o ."
	}
}
