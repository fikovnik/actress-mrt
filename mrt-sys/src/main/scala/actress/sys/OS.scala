package actress.sys

import fr.inria.spirals.actress.metamodel.AClass

trait OSInfo extends AClass {

  def name: String

}

trait OS extends AClass {

  def os: OSInfo

}