package minsait.data

package object azure {
  case class LearningUnit(title: String, href: String, time: String)

  case class Module(title: String, href: String, time: String, units: Seq[LearningUnit] = Nil)

  case class LearningPath(title: String, href: String, time: String = "0:00", modules: Seq[Module] = Nil)
}
