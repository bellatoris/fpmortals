package chapter1

import scala.io.StdIn
import scala.concurrent._
import scala.concurrent.duration.Duration

object `package` {
  type Now[X] = X
}

trait Terminal[C[_]] {
  def read: C[String]
  def write(t: String): C[Unit]
}

object TerminalSync extends Terminal[Now] {
  def read: String = io.StdIn.readLine
  def write(t: String): Unit = println(t)
}

class TerminalAsync(implicit EC: ExecutionContext) extends Terminal[Future] {
  def read: Future[String] = Future { StdIn.readLine }
  def write(t: String): Future[Unit] = Future { println(t) }
}

object TerminalIO extends Terminal[IO] {
  def read: IO[String]           = IO { io.StdIn.readLine }
  def write(t: String): IO[Unit] = IO { println(t) }
}

trait Execution[C[_]] {
  def chain[A, B](c: C[A])(f: A => C[B]): C[B] // flatMap
  def create[B](b: B): C[B] // B => C[B]
}

object Execution {
  implicit class Ops[A, C[_]](c: C[A]) {
    def flatMap[B](f: A => C[B])(implicit e: Execution[C]): C[B] = e.chain(c)(f)
    def map[B](f: A => B)(implicit e: Execution[C]): C[B] = e.chain(c)(f andThen e.create)
  }

  implicit val now: Execution[Now] = new Execution[Now] {
    def chain[A, B](c: A)(f: A => B): B = f(c)
    def create[B](b: B): B = b
  }

  implicit def future(implicit EC: ExecutionContext): Execution[Future] = new Execution[Future] {
    def chain[A, B](c: Future[A])(f: A => Future[B]): Future[B] = c.flatMap(f)
    def create[B](b: B): Future[B] = Future.successful(b)
  }

  implicit val deferred: Execution[IO] = new Execution[IO] {
    def chain[A, B](c: IO[A])(f: A => IO[B]): IO[B] = c.flatMap(f)
    def create[B](b: B): IO[B] = IO(b)
  }
}

object Runner {
  import Execution.Ops
  import ExecutionContext.Implicits._

  def echo[C[_]](t: Terminal[C], e: Execution[C]): C[String] =
    e.chain(t.read) {
      in: String => e.chain(t.write(in)) {
        _: Unit => e.create(in)
      }
    }

  def echo2[C[_]](implicit t: Terminal[C], e: Execution[C]): C[String] =
    t.read.flatMap {
      in: String => t.write(in).map {
        _: Unit => in
      }
    }

  def echo3[C[_]](implicit t: Terminal[C], e: Execution[C]): C[String] =
    for {
      in <- t.read
      _ <- t.write(in)
    } yield in

  implicit val now: Terminal[Now]       = TerminalSync
  implicit val future: Terminal[Future] = new TerminalAsync
  implicit val io: Terminal[IO]         = TerminalIO

  def main(args: Array[String]): Unit = {
    // interpret for Now (impure)
    echo3[Now]: Now[String]

    // interpret for Future (impure)
    val running: Future[String] = echo3[Future]
    Await.result(running, Duration.Inf)

    // define using IO
    val delayed: IO[String] = echo3[IO]
    // interpret, impure, end of the world
    delayed.interpret()
  }
}

final class IO[A](val interpret: () => A) {
  def map[B](f: A => B): IO[B] = IO(f(interpret()))
  def flatMap[B](f: A => IO[B]): IO[B] = IO(f(interpret()).interpret())
}

object IO {
  def apply[A](a: =>A): IO[A] = new IO(() => a)
}
