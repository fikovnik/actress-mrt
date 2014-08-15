package fr.inria.spirals.actress.runtime




// find location of all interfaces
// ask server where are they located
// get all asnwers
// initialize all clients
// wait for clients to be ready

//class OSClientActor(targetPath: String) extends Actor {
//  implicit val timeout = Timeout(5 seconds)
//  import context._
//
//  val identifyId = 1
//  actorSelection(targetPath) ! Identify(identifyId)
//
//  override def receive = {
//    case Status ⇒
//      sender ! NotReady()
//
//    case ActorIdentity(identifyId, Some(ref)) ⇒
//      watch(ref)
//      become(active(ref))
//
//    case ActorIdentity(identifyId, None) ⇒
//      stop(self)
//  }
//
//  def active(target: ActorRef): Actor.Receive = {
//    case Status ⇒ sender ! Ready()
//    case Get("os") ⇒
//      val f = (target ? Get("os")).mapTo[ActorReference]
//      pipe(f) to sender
//    //      val r = Await.result(f, timeout.duration)
//
//    case Terminated(_) ⇒ stop(self)
//  }
//}
//
//object ActressClient {
//
//  def apply(): OS = {
//    val sys = ActorSystem("actress-client")
//
//    val clazz = classOf[OS]
//
//    val handler = new InvocationHandler with Reflection {
//
//      // find what all interfaces are involved
//      val interfaces = clazz.declaredMethods
//        .groupBy(_.declaringClass)
//        .keys
//        .map(_.name)
//        
//      
//
//      override def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
//        val name = method.name
//
//        name match {
//          case "os" ⇒
//        }
//      }
//    }
//
//    JProxy.newProxyInstance(clazz.getClassLoader(), Array(clazz), handler)
//  }
//
//}


//object ActressClientActor {
//  def apply(server: ActorRef): Props = {
//    Props(new ActressClientActor(server))
//  }
//}
//
//class ActressClientActor(server: ActorRef) extends Actor {
//  override def receive = ???
//}
//
////object ActressClient {
////
////  def apply[T: ClassTag](): T = {
////    val clazz = classTag[T].runtimeClass
////    val system = ActorSystem("actress")
////    val client = system.actorOf(ActressClientActor(""))
////    val handler = new ActressHandler(client)
////    val proxy = JProxy.newProxyInstance(clazz.getClassLoader(), Array(clazz), handler)
////
////    proxy.asInstanceOf[T]
////
////  }
////
////}
//
//class ActressHandler(actor: ActorRef) extends InvocationHandler with Reflection {
//
//  override def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
//
//    val name = method.getName
//    method.resolveGenericReturnType match {
//      case Seq(a) ⇒
//      case Seq(a, b) if a == classOf[Option[_]] ⇒ 
//      case Seq(a, b) if a == classOf[Observable[_]] ⇒ 
//      case Seq(a, b) if a == classOf[Traversable[_]] ⇒
//      
//      // TODO: other cases
//      // - Observable[Option[_]]
//      // - Traverable[Option[_]]
//      // - Observable[Traversable[_]]
//      // - Observable[Traversable[Option[_]]]
//      case a ⇒ throw new IllegalStateException("Unsupported type " + a)
//    }
//
//    // is it a collection
//    // is it optional
//    // is it observable
//
//    null
//
//  }
//
//}