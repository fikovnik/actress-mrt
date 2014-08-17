package fr.inria.spirals.actress.runtime

import java.lang.reflect.Method

import actress.core.binding.{ModelEndpointBinding, ModelsEndpointsBinding}
import actress.core.{ModelEndpoint, ModelsEndpoints}
import akka.actor.Actor.Receive
import akka.actor._
import akka.agent.Agent
import fr.inria.spirals.actress.metamodel._
import fr.inria.spirals.actress.runtime.protocol.{AttributeValue, GetReply, Reference, References}
import fr.inria.spirals.actress.util.Reflection._

object MRTClassActor {

  trait FeatureAccessor {
    def get: Method
  }

  case class ReadOnlyFeatureAccessor(get: Method) extends FeatureAccessor

  case class MutableFeatureAccessor(get: Method, set: Method) extends FeatureAccessor

  case class MutableCollectionFeatureAccessor(get: Method, add: Method, del: Method) extends FeatureAccessor

  //  def findAdd(feature: Containment, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
  //    m.returnType == classOf[Unit] &&
  //      m.name == feature.name + "_add"
  //    // TODO: check arguments
  //  }
  //
  //  def findDel(feature: Containment, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
  //    m.returnType == classOf[Unit] &&
  //      m.name == feature.name + "_del"
  //    // TODO: check arguments
  //  }

  //  def findSet(feature: Containment, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
  //    m.returnType == classOf[Unit] &&
  //      (m.name == feature.name + "_$eq" || m.name == "set" + feature.name.capitalize) &&
  //      m.parameterTypes.size == 1
  //    // TODO: check arguments
  //  }

  def findGet(feature: AFeature, clazz: Class[_]): Option[Method] = clazz.declaredMethods find { m =>
    m.name == feature._name && m.parameterTypes.size == 0
    //      if (feature._many) {
    //        // TODO: check return type - in this case it is a collection of strings
    //      } else {
    //        // TODO: check return type
    //        true
    //      }
    //    }
  }

  def loadFeatures(model: AClass, bindingFactory: BindingFactory): Map[String, (AFeature, FeatureAccessor)] = {
    val bndClazz = classOf[BindingFactory].declaredMethods.find(_.name == "binding").map(_.returnType).get

    model._features.map { f =>
      // TODO: handle exceptions in cases given methods are not found

      val get = findGet(f, bndClazz).get
      //      val accessor = (f.container, f.mutable) match {
      //        case (true, true) =>
      //          MutableCollectionFeatureAccessor(get, findAdd(f, bndClazz).get, findDel(f, bndClazz).get)
      //        case (false, true) =>
      //          MutableFeatureAccessor(get, findSet(f, bndClazz).get)
      //        case (_, false) =>
      val accessor = ReadOnlyFeatureAccessor(get)
      //      }

      f._name -> ((f, accessor))
    }
      .toMap
  }

}

class PackageModelActor(pkg: APackage) extends Actor with ActorLogging {

  import context._

  override def preStart(): Unit = {

    pkg._classes foreach { c =>
      log info s"$this: starting model actor for model $c"

      actorOf(Props(new ModelActor(c)), c._name)
    }

  }

  override def receive = {
    case "" =>
  }

}

class ModelActor(model: AClass) extends Actor with ActorLogging {

  import context._

  override def preStart(): Unit = {

    model._references filter (_._containment) foreach { r =>

      log info s"$this: starting model actor for contained reference $r"

      actorOf(Props(new ModelActor(r._type)), r._name)

    }

  }

//  val features = loadFeatures(model)

//  def binding(elementId: String): U = bindingFactory(elementId)

  override def receive = {
    case msg@protocol.Get(name, elementId) ⇒
//      log debug s"Get $msg for $elementId"
//
//      features.get(name) match {
//        case Some((feature, accessor)) =>
//          sender ! doGet(feature, accessor, binding(elementId))
//        case None =>
//          log warning s"$name: unknown attribute"
//          sender ! protocol.UnknownAttribute(name, elementId)
//      }
  }

//  def endpointFor(clazz: AClass) = endpoints()(clazz._name)
//
//  def doGet(feature: AFeature, accessor: FeatureAccessor, binding: U): GetReply = {
//    // TODO: handle failure
//    val res = accessor.get.invoke(binding)
//
//    feature match {
//      case _: AAttribute =>
//        AttributeValue(feature._name, res)
//
//      case ref: AReference =>
//        // TODO: handle local endpoints?
//        val endpoint = endpointFor(ref._type)
//
//        if (ref._many) {
//          References(res.asInstanceOf[Set[String]], endpoint)
//        } else {
//          Reference(res.asInstanceOf[String], endpoint)
//        }
//    }
//  }
}

class ActressServer {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val sys = ActorSystem("actress-server")

  // top level models
  // TODO: it should be AClass instead of String, but for this we need a registry of all AClasses
  private val endpoints = Agent(Map[String, ActorRef]())

//  private val bindingRegistry = Agent(Map[AClass, BindingFactory]())

  val modelsEndpoints = {

    object ModelsEndpointsPackage extends APackageImpl {

      val ModelsEndpoints = APackage.registry.aClass[ModelsEndpoints]
      val ModelEndpoint = APackage.registry.aClass[ModelEndpoint]

      _name = "modelsendpoints"
      _classifiers ++= Seq(
        ModelsEndpoints,
        ModelEndpoint
      )

    }

    val modelEndpointBF = new BindingFactory {
      def apply(elementId: String) = {

        // TODO: check for None for elementId and endpoint
        val endpoint = endpoints()(elementId)
        ModelEndpointBinding(elementId, endpoint)
      }
    }

    val modelsEndpointsBF = new BindingFactory {
      def apply(elementId: String) = new ModelsEndpointsBinding(endpoints)
    }

    val bindingMapper = { model: AClass =>
      model match {
        case ModelsEndpointsPackage.ModelsEndpoints => modelsEndpointsBF
        case ModelsEndpointsPackage.ModelEndpoint => modelEndpointBF
      }
    }

    val ref = registerPackage(ModelsEndpointsPackage, bindingMapper)

    // create a new instance of a model endpoint
    // add it to modelsenspoints
    // -> ref ! Create(reference)
    // <- Reference
    // ref ! Set()
    // ref ! Add(elementId, reference, instance)


    ref
  }

  //  def registerBinding[T <: AClass](clazz: T, factory: BindingFactory[T]): Unit = {
  //    bindingRegistry send (_ + (clazz -> factory))
  //  }
  //
  //  def registerModel(model: AClass): ActorRef = {
  //    val ref = sys.actorOf(Props(new MRTClassActor[T](endpoints, bindingFactory)), name)
  //
  //    endpoints send (_ + (name -> ref))
  //
  //    ref
  //  }
  //
  //  def registerModel[T <: AClass : ClassTag]: ActorRef = registerModel(APackageRegistry.aClass[T])

  def registerPackage(pkg: APackage, bindingMapper: (AClass => BindingFactory)): ActorRef = {

    sys.actorOf(Props(new PackageModelActor(pkg)))

  }

}