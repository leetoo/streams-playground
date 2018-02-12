import scala.collection.JavaConverters._


object SmallestPositiveInteger {
  def solution(a: Array[Int]): Int = {
    /**
      * Print smallest positive (> 0) integer that does not occur in A
      */
    val as = a.toSet
    val max = as.max
    val range = (1 to as.max).toSet
    val diff = (1 to max).toSet diff as
    if (range == as)
      max + 1
    else
      if (diff.nonEmpty) diff.min
      else 1
  }

//  val s = Array(1, 3, 6, 4, 1, 2).toSet
  val s = Array(-1, -3).toSet
  val range = (1 to s.max).toSet
  range diff s
//  println(solution(Array(1, 3, 6, 4, 1, 2)))
}

object BinaryGap {
  def solution(n: Int): Int = {

    val binaryStr = n.toBinaryString
    assert(binaryStr.forall(Binary.fromChar(_).isDefined), "OOPS")

    val binaryRepr: Array[Binary] = binaryStr.toCharArray.map(c => Binary.fromChar(c).get)

    val gaps = binaryRepr.foldLeft(List[Int]())(gapChecker)

    //The last gap is closed only if the nr ends with one
    if (binaryRepr.last == Binary.One)
      gaps.max
    else
      gaps.tail.max
  }

  sealed trait Binary
  object Binary {
    object One extends Binary
    object Zero extends Binary

    def fromChar(c: Char): Option[Binary] = c match {
      case '0'=> Some(Zero)
      case '1' => Some(One)
      case _ => None
    }
  }

  type Gaps = List[Int]

  def gapChecker(gaps: Gaps, next: Binary): Gaps = (next, gaps) match {
    case (Binary.Zero, Nil) => 0 :: Nil
    case (Binary.Zero, g :: gs) => g + 1 :: gs
    case (Binary.One, gs) => 0 :: gs
  }

}
