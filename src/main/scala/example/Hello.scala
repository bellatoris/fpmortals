package example

import scalaz._, Scalaz._
import simulacrum._

object Hello extends App {
  type Now[X] = X
}

import Hello._

trait Terminal[C[_]] {
  def read: C[String]
  def write(t: String): C[Unit]
}

object TerminalSync extends Terminal[Now] {
  def read: String = io.StdIn.readLine
  def write(t: String): Unit = println(t)
}

// object TerminalAsync extends Terminal[Future] {
//   def read: Future[String] = ???
//   def write(t: String): Future[Unit] = ???
// }

object TerminalIO extends Terminal[IO] {
  def read: IO[String]           = IO { io.StdIn.readLine }
  def write(t: String): IO[Unit] = IO { println(t) }
}

final class IO[A](val interpret: () => A) {
  def map[B](f: A => B): IO[B] = IO(f(interpret()))
  def flatMap[B](f: A => IO[B]): IO[B] = IO(f(interpret()).interpret())
}

object IO {
  def apply[A](a: =>A): IO[A] = new IO(() => a)
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
}
