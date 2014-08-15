package fr.inria.spirals.actress.util

import java.lang.reflect.Type
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.annotation.Annotation
import scala.reflect.ClassTag
import scala.reflect.classTag

object Reflection extends Reflection

trait Reflection {

  implicit class RichClass(that: Class[_]) {
    def name: String = that.getName()
    def declaredMethods: Seq[Method] = that.getDeclaredMethods()
  }

  implicit class RichParameterizedType(that: ParameterizedType) {
    def rawType: Type = that.getRawType
    def actualTypeArguments: Seq[Type] = that.getActualTypeArguments
  }
  
  object Annotated {
	  def unapply[T <: Annotation : ClassTag](m: Method): Option[T] = {
			  if (m.hasAnnotation[T]) Some(m.annotation[T])
			  else None
	  }
  }
  
  implicit class RichMethod(that: Method) {

    type ParameterizedType = java.lang.reflect.ParameterizedType
    object ParameterizedType {
      def unapply(t: Type): Option[(Class[_], Option[Type])] = t match {
        case p: ParameterizedType ⇒ p.rawType match {
          case c: Class[_] ⇒ Some((c, p.actualTypeArguments.headOption))
          case _ ⇒ None
        }
        case _ ⇒ None
      }
    }
    
    def name: String = that.getName
    def genericReturnType: Type = that.getGenericReturnType
    def declaringClass: Class[_] = that.getDeclaringClass
    def annotation[T <: Annotation : ClassTag]: T = that.getAnnotation(classTag[T].runtimeClass.asInstanceOf[Class[Annotation]]).asInstanceOf[T]
    def hasAnnotation[T <: Annotation : ClassTag] = that.isAnnotationPresent(classTag[T].runtimeClass.asInstanceOf[Class[Annotation]])
    
    def resolveGenericReturnType: Seq[Class[_]] = {
      def resolve(t: Type, acc: Seq[Class[_]]): Seq[Class[_]] = t match {
        case ParameterizedType(raw, Some(actual)) ⇒ resolve(actual, acc :+ raw)
        case ParameterizedType(raw, None) ⇒ acc :+ raw
        case c: Class[_] ⇒ acc :+ c
      }

      resolve(that.genericReturnType, Seq())
    }
  }

}