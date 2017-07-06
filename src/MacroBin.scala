import scala.collection.mutable.ArrayBuffer

class MacroBin (lowerBound:Double, uperBound:Double){
  val microBinsIDs = ArrayBuffer[Integer]()
  val pointsIDs = ArrayBuffer[Integer]()
  var mdh:Array[Integer] = null
  var numMDHs = 0
  var dataMatrix:DataMatrix = null
  var means:Array[Double] = null
  var devs:Array[Double] = null
  var name:String = null  
}