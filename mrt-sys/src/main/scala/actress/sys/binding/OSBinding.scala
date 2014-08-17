package actress.sys.binding

import actress.sys.OSInfo
import fr.inria.spirals.actress.runtime.Binding

class OSInfoBinding extends Binding {

  def name = System.getProperty("os.name")

}

