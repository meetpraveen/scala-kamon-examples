package akka

import akka.actor.{ Actor, ActorRef }
import akka.util.Timeout
import com.meetpraveen.log.LogContext
import org.slf4j.MDC

import scala.concurrent.Future

trait MDCAwareActor extends Actor with LogContext {
  import MDCAwareActor._

  // This is why this needs to be in package akka.actor
  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    //    val orig = MDC.getCopyOfContextMap
    try {
      msg match {
        case MdcMsg(mdc, origMsg) =>
          //          if (mdc != null)
          //            MDC.setContextMap(mdc)
          //          else
          //            MDC.clear()
          super.aroundReceive(receive, origMsg)
        case _ =>
          super.aroundReceive(receive, msg)
      }
    } finally {
      //      if (orig != null)
      //        MDC.setContextMap(orig)
      //      else
      //        MDC.clear()
    }
  }
}

object MDCAwareActor {
  private case class MdcMsg(mdc: java.util.Map[String, String], msg: Any)

  object Implicits {

    /**
     * Add two new methods that allow MDC info to be passed to MDCContextAware actors.
     *
     * Do NOT use these methods to send to actors that are not MDCContextAware.
     */
    implicit class ContextLocalAwareActorRef(val ref: ActorRef) extends AnyVal {

      import akka.pattern.ask

      /**
       * Send a message to an actor that is MDCContextAware - it will propagate
       * the current MDC values.
       */
      def !!(msg: Any): Unit =
        ref ! MdcMsg(MDC.getCopyOfContextMap, msg)

      /**
       * "Ask" an actor that is MDCContextAware for something - it will propagate
       * the current MDC values
       */
      def ??(msg: Any)(implicit timeout: Timeout): Future[Any] =
        ref ? MdcMsg(MDC.getCopyOfContextMap, msg)
    }
  }
}
