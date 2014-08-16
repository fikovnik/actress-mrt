package fr.inria.spirals.actress.runtime

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

import actress.core.{ModelEndpoint, ModelsEndpoints}
import actress.core.binding.{ModelEndpointBinding, ModelsEndpointsBinding}
import akka.actor._
import fr.inria.spirals.actress.metamodel.{MRTFeature, MRTClass, Observable}
import fr.inria.spirals.actress.runtime.protocol.{AttributeValue, GetReply, Reference, References}
import fr.inria.spirals.actress.util.Reflection._

import scala.collection.JavaConversions._
import scala.reflect.{ClassTag, classTag}

object MRTClassActor {

  trait FeatureAccessor {
    def get: Method
  }

  case class ReadOnlyFeatureAccessor(get: Method) extends FeatureAccessor

  case class MutableFeatureAccessor(get: Method, set: Method) extends FeatureAccessor

  case class MutableCollectionFeatureAccessor(get: Method, add: Method, del: Method) extends FeatureAccessor

  def findAdd(feature: MRTFeature, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
    m.returnType == classOf[Unit] &&
      m.name == feature.name + "_add"
    // TODO: check arguments
  }

  def findDel(feature: MRTFeature, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
    m.returnType == classOf[Unit] &&
      m.name == feature.name + "_del"
    // TODO: check arguments
  }

  def findSet(feature: MRTFeature, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
    m.returnType == classOf[Unit] &&
      (m.name == feature.name + "_$eq" || m.name == "set" + feature.name.capitalize) &&
      m.parameterTypes.size == 1
    // TODO: check arguments
  }

  def findGet(feature: MRTFeature, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
    m.name == feature.name && {
      if (feature.container) {
        m.parameterTypes.size == 0
        // TODO: check return type - in this case it is a collection of strings
      } else {
        // TODO: check return type
        true
      }
    }
  }

  def loadFeatures[T <: MRTClass : ClassTag, U <: Binding[T] : ClassTag]: Map[String, (MRTFeature, FeatureAccessor)] = {
    val bndClazz = classTag[U].runtimeClass

    inspectFeatures[T]
      .map { f =>
      // TODO: handle exceptions in cases given methods are not found

      val get = findGet(f, bndClazz).get
      val accessor = (f.container, f.mutable) match {
        case (true, true) =>
          MutableCollectionFeatureAccessor(get, findAdd(f, bndClazz).get, findDel(f, bndClazz).get)
        case (false, true) =>
          MutableFeatureAccessor(get, findSet(f, bndClazz).get)
        case (_, false) =>
          ReadOnlyFeatureAccessor(get)
      }

      f.name -> ((f, accessor))
    }
      .toMap
  }

  def inspectFeatures[T <: MRTClass : ClassTag]: Seq[MRTFeature] = {

    object ReferenceType {
      def unapply(clazz: Class[_]): Option[Boolean] =
        if (classOf[MRTClass] <:< clazz) Some(true)
        else Some(false)
    }

    object CollectionType {
      def unapply(clazz: Class[_]): Option[(Boolean, Boolean, Boolean)] =
        if (classOf[Set[_]] <:< clazz) Some((false, false, true))
        else if (classOf[collection.mutable.Set[_]] <:< clazz) Some((true, false, true))
        else None
    }

    val `Observable[]` = classOf[Observable[_]]

    val mrtClazz = classTag[T].runtimeClass

    val candidates = mrtClazz.declaredMethods

    // do not confuse with the findSet which searches the binding class
    def hasSetter(feature: MRTFeature) = mrtClazz.declaredMethods exists { m =>
      m.returnType == classOf[Unit] &&
        (m.name == feature.name + "_$eq" || m.name == "set" + feature.name.capitalize) &&
        m.parameterTypes.size == 1
      // TODO: check arguments
    }

    for (m <- candidates) yield m.resolveGenericReturnType match {
      // 1..1 feature
      case Seq(rawType@ReferenceType(reference)) =>
        val f = MRTFeature(m.name, rawType, reference = reference)
        f copy (mutable = hasSetter(f))

      // 1..1 observable feature
      case Seq(`Observable[]`, rawType@ReferenceType(reference)) =>
        val f = MRTFeature(m.name, rawType, reference = reference, container = false, observable = true)
        f copy (mutable = hasSetter(f))

      // 0..* features
      case Seq(`Observable[]`, CollectionType(mutable, ordered, unique), rawType@ReferenceType(reference)) =>
        MRTFeature(m.name, rawType, reference = reference, mutable = mutable, container = true, observable = true, ordered = ordered, unique = unique)

      case _ =>
        throw new IllegalArgumentException(s"$m.name: unsupported return type")
    }
  }
}

class MRTClassActor[T <: MRTClass : ClassTag, U <: Binding[T] : ClassTag](endpoints: collection.Map[String, ActorRef], bindingFactory: BindingFactory[T, U]) extends Actor with ActorLogging {

  import fr.inria.spirals.actress.runtime.MRTClassActor._

  // in the future, there could be different binding scopes (similarly to JEE beans)
  // currently all scopes are request based, but there could be
  // - an application scope (a scope shared with all actors during the life type of the actor system)
  //   it is a bit dangerous as it can easily break the actor encapsulation and it has to be thread safe
  // - an actor scope (a scope shared by all requests within the same actor)
  // - a session scope (a scope shared by all requests from a given client and responded by the same actor)
  //   this will have to get coupled with a new routing policy having one actor per client


  //  val features: Map[String, DynamicFeature] = {
  //
  //  }

  val features = loadFeatures[T, U]

  def binding(elementId: Option[String]): U = bindingFactory(elementId)

  override def receive = {
    case msg@protocol.Get(name, elementId) ⇒
      log debug s"Get $msg for $elementId"

      features get name match {
        case Some((feature, accessor)) =>
          sender ! doGet(feature, accessor, binding(elementId))
        case None =>
          log warning s"$name: unknown attribute"
          sender ! protocol.UnknownAttribute(name, elementId)
      }
  }

  def endpointFor(clazz: Class[_]) = endpoints(clazz.name)

  def doGet(feature: MRTFeature, accessor: FeatureAccessor, binding: U): GetReply = {
    // TODO: handle failure
    val res = accessor.get.invoke(binding)

    if (feature.reference) {

      val endpoint = endpointFor(feature.rawType)

      if (feature.container) References(res.asInstanceOf[Set[String]], endpoint)
      else Reference(res.asInstanceOf[String], endpoint)

    } else {
      AttributeValue(feature.name, res)
    }
  }
}

class ActressServer {

  private val sys = ActorSystem("actress-server")
  private val endpoints = new ConcurrentHashMap[String, ActorRef]()

  val modelsEndpoints = {
    // deploy ModelEndpoint

    registerMRTClass(new BindingFactory[ModelEndpoint, ModelEndpointBinding] {
      def apply(elementId: Option[String]) = {

        // TODO: check for None for elementId and endpoint
        val endpoint = endpoints(elementId.get)
        ModelEndpointBinding(elementId.get, endpoint)
      }
    })

    registerMRTClass(new BindingFactory[ModelsEndpoints, ModelsEndpointsBinding] {
      // application scope binding
      // see the comments in ModelsEndpointsBinding
      val binding = new ModelsEndpointsBinding(endpoints.keySet)

      def apply(elementId: Option[String]) = binding
    })
  }

  def registerMRTClass[T <: MRTClass : ClassTag, U <: Binding[T] : ClassTag](bindingFactory: BindingFactory[T, U]): ActorRef = {
    val name = classTag[T].runtimeClass.name
    val ref = sys.actorOf(Props(new MRTClassActor[T, U](endpoints, bindingFactory)), name)

    endpoints += name -> ref

    ref
  }

}