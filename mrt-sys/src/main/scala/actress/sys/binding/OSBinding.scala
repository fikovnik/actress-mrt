package actress.sys.binding

import actress.sys.OSInfo
import fr.inria.spirals.actress.runtime.Binding

class OSInfoBinding extends Binding[OSInfo] {

  def name = System.getProperty("os.name")

}

