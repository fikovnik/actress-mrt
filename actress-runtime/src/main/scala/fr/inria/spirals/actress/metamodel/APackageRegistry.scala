package fr.inria.spirals.actress.metamodel

trait APackageRegistry extends AObject {

  @Containment
  def _packages: AMutableSequence[APackage]

}
