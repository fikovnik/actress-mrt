package fr.inria.spirals.actress.acore.util

import fr.inria.spirals.actress.acore.AObject
import fr.inria.spirals.actress.acore.impl.AObjectImpl

trait ContainmentSettingIterable[A <: AObject] extends Iterable[A] {

  def containmentPair: ContainmentPair

  abstract override def iterator: Iterator[A] = new Iterator[A] {
    val underlying = ContainmentSettingIterable.super.iterator

    override def hasNext: Boolean = underlying.hasNext

    override def next(): A = {
      val elem = underlying.next()

      elem.asInstanceOf[AObjectImpl].__containmentPair = Some(containmentPair)
      elem._endpoint
      elem

    }
  }
}
