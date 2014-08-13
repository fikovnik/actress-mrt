package actress.sys

import fr.inria.spirals.actress.metamodel.Attribute

trait OSInfo {

  @Attribute
  def name: String
  
}

trait OS {
  
  def os: OSInfo
  
}