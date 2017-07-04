import scala.collection.mutable.ArrayBuffer

class DataMatrix 
{
  var rows = 0
  var cols = 0
  var catCols = 0
  var data = ArrayBuffer[DataPoint]()
  
  def this(inCols:Int,inCatCols:Int)
  {
    this()
    cols = inCols
    catCols = inCatCols
  }
  
  def this(inRows:Int,inCols:Int,inCatCols:Int)
  {
    this()
    rows = inRows
    cols = inCols
    catCols = inCatCols
  }
  
  def this(inRows:Int,inCols:Int,inCatCols:Int,inData:ArrayBuffer[DataPoint])
  {
    this()
    rows = inRows
    cols = inCols
    catCols = inCatCols
    data = inData
  }
  
}