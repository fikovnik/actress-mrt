package actress.sys

import fr.inria.spirals.actress.metamodel.Reference
import fr.inria.spirals.actress.metamodel.Attribute
import fr.inria.spirals.actress.runtime.Binding

class OSInfoBinding extends OSInfo with Binding[OSInfo] {
  
  @Attribute
  def name = System.getProperty("os.name")

}

class OSBinding {
  
  @Reference
  def os = new OSInfoBinding
  
}