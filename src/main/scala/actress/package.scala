import fr.inria.spirals.actress.metamodel.Observable

package object actress {

  implicit def observable2type[T](that: Observable[T]): T = that()
  
}