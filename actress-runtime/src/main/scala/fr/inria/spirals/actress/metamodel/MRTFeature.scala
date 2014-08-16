package fr.inria.spirals.actress.metamodel

import scala.language.existentials

case class MRTFeature(
  name: String,
  rawType: Class[_],
  reference: Boolean = false,
  mutable: Boolean = false,
  container: Boolean = false,
  unique: Boolean = false,
  ordered: Boolean = false)