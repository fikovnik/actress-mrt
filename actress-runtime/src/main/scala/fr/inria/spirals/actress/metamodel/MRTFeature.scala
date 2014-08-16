package fr.inria.spirals.actress.metamodel

case class MRTFeature(
  name: String,
  rawType: Class[_],
  reference: Boolean = false,
  required: Boolean = false,
  observable: Boolean = false,
  mutable: Boolean = false,
  container: Boolean = false,
  unique: Boolean = false,
  ordered: Boolean = false)