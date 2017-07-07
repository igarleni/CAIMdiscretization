import scala.collection.mutable.ArrayBuffer

class DataPoint (val numMeasures:Int,val numCatContexts:Int)
{
  var measures = if (numMeasures > 0) new Array[Double](numMeasures) else Array[Double]()
  var catContexts = if (numCatContexts > 0) new Array[String](numCatContexts) else Array[String]()
  var universalID = 0
  var localID = 0
  
  var neighbors = ArrayBuffer[Int]()
  var distToNeighbors = ArrayBuffer[Double]()
  var lrd = 0.0
  var classID = ""
  
  def this(p:DataPoint, numMeasures:Int, numCatContexts:Int)
  {
    this(numMeasures, numCatContexts)
    for(i <- 0 until numMeasures) measures(i) = p.measures(i)
    for(i <- 0 until numCatContexts) catContexts(i) = p.catContexts(i)
  }
}