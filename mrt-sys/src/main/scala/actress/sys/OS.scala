package actress.sys

import fr.inria.spirals.actress.metamodel.{MRTClass}

trait OSInfo extends MRTClass {

  def name: String

}

trait OS extends MRTClass {

  def os: OSInfo

}