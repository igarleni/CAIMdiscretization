import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object DataReader {
  
  def readData(filename:String, dataMatrix:DataMatrix, singleContexts: Array[ArrayBuffer[String]]): Unit =
  {
    val source = Source.fromFile(filename,"UTF-8")
    val lineIterator = source.getLines
    
    var countUniversalID = 0
    Constants.MAX_COLS = new Array[Double](dataMatrix.cols)
	  Constants.MIN_COLS = new Array[Double](dataMatrix.cols)
	  Constants.DATA_MEANS = new Array[Double](dataMatrix.cols)
	  Constants.DATA_DEVS = new Array[Double](dataMatrix.cols)
	  Constants.CRES = new Array[Double](dataMatrix.cols)
	  for(i <- 0 until dataMatrix.cols)
	  {
	    Constants.MAX_COLS(i) = -Double.MaxValue
	    Constants.MIN_COLS(i) = Double.MaxValue
	    Constants.DATA_MEANS(i) = 0
	    Constants.DATA_DEVS(i) = 0
	  }
    
    for(l <- lineIterator if !l.contains('?'))
    {
      val splitLine = l.split(Constants.FIELD_DELIMITER)
      if (splitLine.length != (dataMatrix.catCols + dataMatrix.cols + 1))
        throw new Exception("Invalid Input!: " + l);
      
      /*Creamos el nuevo punto*/
      val newPoint = new DataPoint(dataMatrix.cols, dataMatrix.catCols)
      newPoint.universalID = countUniversalID
      countUniversalID += 1
      
      /*Obtenemos la variable Clase del punto, si es conocido seguimos, si no, se guarda en Constants*/
      newPoint.classID = new String(splitLine(dataMatrix.catCols + dataMatrix.cols))
      var exist = false
      for (i <- Constants.CLASS_LABELS if i.equals(newPoint.classID)) exist = true
      if (!exist)
        Constants.CLASS_LABELS.+= (newPoint.classID);
      
      /*Leemos los datos de las dimensiones numericas del punto nuevo y calculamos
      el maximo de cada dimension al mismo tiempo*/
      for(i <- 0 until dataMatrix.cols)
      {
        newPoint.measures(i) = splitLine(i).toDouble
        if (newPoint.measures(i) > Constants.MAX_COLS(i)) 
          Constants.MAX_COLS(i) = newPoint.measures(i);
        if (newPoint.measures(i) < Constants.MIN_COLS(i)) 
          Constants.MIN_COLS(i) = newPoint.measures(i);
      }
      
      /*Leemos los datos de las dimensiones categoricas del punto y creamos una lista de los posibles
      valores de cada dimension en singleContexts*/
      for(i <- 0 until dataMatrix.catCols)
      {
        newPoint.catContexts(i) = splitLine(i + dataMatrix.cols)
        val contextsSize = singleContexts(i + dataMatrix.cols).length
        var exists = false
        for (j <- 0 until contextsSize if (singleContexts(i + dataMatrix.cols)(j).equals(newPoint.catContexts(i))))
        {
          exists = true
        }
        
      }
      
      dataMatrix.data += newPoint
      
    }
    
    /*Calculo de la media de cada dimension numerica*/
    for (i <- 0 until dataMatrix.rows)
    {
      val newPoint = dataMatrix.data(i)
      for (j <- 0 until dataMatrix.cols)
      {
        if (Constants.MAX_COLS(j) == Constants.MIN_COLS(j))
          newPoint.measures(j) = -Constants.MAX_VAL
        else
          newPoint.measures(j) = 2 * Constants.MAX_VAL * (newPoint.measures(j) - Constants.MIN_COLS(j)) / (Constants.MAX_COLS(j) - Constants.MIN_COLS(j)) - Constants.MAX_VAL;
        
        if(newPoint.measures(j) > Constants.MAX_VAL)
          newPoint.measures(j) = Constants.MAX_VAL
        else if (newPoint.measures(j) < -Constants.MAX_VAL)
          newPoint.measures(j) = - Constants.MAX_VAL;
        
        Constants.DATA_MEANS(j) += newPoint.measures(j) / dataMatrix.rows
      }
    }
    /*Calculo de DEVS y CRES de cada dimension numerica*/
    
    /*Ordenamos los puntos segun CRES*/
    
    /*Guardamos el numero de filas, por si el usuario los ha pasado mal*/
    dataMatrix.rows = countUniversalID
    Constants.NUM_ROWS = countUniversalID
    source.close
  }
  
}