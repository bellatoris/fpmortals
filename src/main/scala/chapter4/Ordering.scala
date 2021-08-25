package chapter4

trait Ordering[T] {
  def compare(x: T, y: T): Int

  def lt(x: T, y: T): Boolean = compare(x, y) < 0
  def gt(x: T, y: T): Boolean = compare(x, y) > 0
}

trait Numeric[T] extends Ordering[T] {
  def plux(x: T, y: T): T
  def times(x: T, y: T): T
  def negate(x: T): T
  def zero: T

  def abs(x: T): T = if (lt(x, zero)) negate(x) else x
}
