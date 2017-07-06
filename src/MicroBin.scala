import scala.collection.mutable.ArrayBuffer

class MicroBin (lowerBound:Double, upperBound:Double){
  val distinctValues = ArrayBuffer[Double]()
  val distinctValueSupports = ArrayBuffer[Integer]()
  val allValues = ArrayBuffer[Double]()
  val pointIDs  = ArrayBuffer[Integer]()
  var dataMatrix:DataMatrix = null
  val dims = ArrayBuffer[Integer]()
  var means:Array[Double] = null
  var devs:Array[Double] = null
}