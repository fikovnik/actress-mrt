package fr.inria.spirals.actress.acore

trait APackageRegistry extends AObject {

  @Containment
  def _packages: AMutableSequence[APackage]

}
