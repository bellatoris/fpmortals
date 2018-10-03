package chapter2

import scala.reflect.runtime.universe._


object Main {
  def main(args: Array[String]): Unit = {
    syntaxSugar()
    assignment()
    filter()
    patternMatching
    patternMatching2
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
}
