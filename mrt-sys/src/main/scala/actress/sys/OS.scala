package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass, MRTFeature}

trait OSInfo extends MRTClass {

  @MRTFeature
  def name: String

}

trait OS extends MRTClass {

  @MRTFeature
  def os: OSInfo

}