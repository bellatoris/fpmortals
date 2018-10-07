package chapter2

import scala.reflect.runtime.universe._
import scalaz._, Scalaz._
import scala.concurrent._
import ExecutionContext.Implicits.global


object Main {
  def main(args: Array[String]): Unit = {
    syntaxSugar()
    assignment()
    filter()
    patternMatching()
    patternMatching2()
    foreach()
    future()
    println(either())
    try {
      futureFail()
    } catch {
      case e: Throwable => println(e.getMessage())
    }
    earlyExit(1)
    try {
      earlyExit(-1)
    } catch {
      case e: Throwable => println(e.getMessage())
    }

    earlyExit2(1)
    earlyExit2(-1)

    monadTransform()
    monadTransform2()
  }

  def syntaxSugar(): Unit = {
    val a, b, c = Option(1)
    println(show {
      reify {
        for {
          i <- a
          j <- b
          k <- c
        } yield (i + j + k)
      }
    })
  }

  def assignment(): Unit = {
    val a, b, c = Option(1)
    println(show {
      reify {
        for {
          i <- a
          j <- b
          ij = i + j
          ijj = i + j + j
          k <- c
        } yield (ij + k)
      }
    })
  }

  def filter(): Unit = {
    val a, b, c = Option(1)
    println(show {
      reify {
        for {
          i <- a
          j <- b
          if i > j
          k <- c
        } yield (i + j + k)
      }
    })
  }

  def patternMatching(): Unit = {
    val a = Option(1)
    println(show {
      reify {
        for {
          i: Int <- a
        }
        yield i
      }
    })
  }

  def patternMatching2(): Unit = {
    trait Tree {}
    case class Leaf() extends Tree() {}
    case class NonLeaf() extends Tree() {}
    
    val a: Some[Leaf] = Some(Leaf())
    println(show {
      reify {
        for {
          i: Leaf <- a
        }
        yield i
      }
    })
  }

  def foreach(): Unit = {
    val a, b, c = Option(1)
    println(show {
      reify {
        for {
          i <- a
          j <- b
          k <- c
        } println(s"$i $j")
      }
    })
  }

  def future(): Unit = {
    var a = 3
    def expensiveCalc(): Int = {
      println(s"a is always 3, a = $a")
      a += 1
      a
    }
    def anotherExpensiveCalc(): Int = {
      println(s"a is always 4, a = $a")
      a += 1
      a
    }

    for {
      i <- Future { expensiveCalc() }
      j <- Future { anotherExpensiveCalc() }
    } yield (i + j)

    java.util.concurrent.TimeUnit.MILLISECONDS.sleep(1)

    a = 3
    val c = Future { expensiveCalc() }
    val d = Future { anotherExpensiveCalc() }
    for { i <- c ; j <- d } yield (i + j)
  }

  def either(): Either[String, Int] = {
    val a = Right(1)
    val b = Right(2)
    val c: Either[String, Int] = Left("sorry, no c")

    for { i <- a; j <- b; k <- c } yield i + j + k
  }

  def futureFail(): Unit = {
    val f = for {
      i <- Future.failed[Int](new Throwable)
      j <- Future { println("hello") ; 1 }
    } yield (i + j)

    Await.result(f, duration.Duration.Inf)
  }

  def earlyExit(i: Int): Unit = {
    def getA: Future[Int] = Future { println("hello") ; i }
    def error(msg: String): Future[Nothing] = Future.failed(new RuntimeException(msg))

    val f =for {
      a <- getA
      b <- if (a <= 0) error(s"$a must be positivie")
           else Future.successful(a)
    } yield b * 10

    Await.result(f, duration.Duration.Inf)
  }

  def earlyExit2(i: Int): Unit = {
    def getA: Future[Int] = Future { println("hello") ; i }
    def getB: Future[Int] = Future { println("bye") ; 1 }

    val f = for {
      a <- getA
      c <- if (a <= 0) 0.pure[Future]
           else for { b <- getB } yield a * b
    } yield c

    Await.result(f, duration.Duration.Inf)
  }

  def monadTransform(): Unit = {
    def getA: Future[Option[Int]] = Future { Option(3) }
    def getB: Future[Option[Int]] = Future { Option(4) }
    def getC: Future[Int] = Future{ 3 }
    def getD: Option[Int] = Option(4)

    val result = for {
      a <- OptionT(getA)
      b <- OptionT(getB)
      c <- getC.liftM[OptionT]
      d <- OptionT(getD.pure[Future])
    } yield (a * b) / (c * d)

    val f = Await.result(result.run, duration.Duration.Inf)
    println(f)
  }

  
  def liftFutureOption[A](f: Future[Option[A]]) = OptionT(f)
  def liftFuture[A](f: Future[A]) = f.liftM[OptionT]
  def liftOption[A](o: Option[A]) = OptionT(o.pure[Future])
  def lift[A](a: A)               = liftOption(Option(a))

  def monadTransform2(): Unit = {
    def getA: Future[Option[Int]] = Future { Option(3) }
    def getB: Future[Option[Int]] = Future { Option(4) }
    def getC: Future[Int] = Future{ 3 }
    def getD: Option[Int] = Option(4)

    val result = for {
      a <- getA    |> liftFutureOption
      b <- getB    |> liftFutureOption
      c <- getC    |> liftFuture
      d <- getD    |> liftOption
      e <- 10      |> lift
    } yield e * (a * b) / (c * d)

    val f = Await.result(result.run, duration.Duration.Inf)
    println(f)
  }
}
