import java.time.LocalDateTime

trait PrintThread {

  def printThread(id: String) = {
    val thread = Thread.currentThread.getName
    println(s"[$thread] $id")
  }
}
