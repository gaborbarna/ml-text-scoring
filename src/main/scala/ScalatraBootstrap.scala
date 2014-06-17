import _root_.akka.actor.{ActorSystem, Props}
import scoring._
import org.scalatra._
import javax.servlet.ServletContext


class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()
  val scoringActor = system.actorOf(Props[ScoringActor])

  override def init(context: ServletContext) {
    context.mount(new ScoringScalatraService(system, scoringActor), "/*")
  }

  override def destroy(context: ServletContext) {
    system.shutdown()
  }
}
