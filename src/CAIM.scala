import scala.collection.mutable.ArrayBuffer

object CAIM {
  
  def discretizeData(dataMatrix:DataMatrix):Array[ArrayBuffer[MacroBin]] = {
    val ret = new Array[ArrayBuffer[MacroBin]](dataMatrix.cols)
    
    /*Aplicamos el algoritmo a cada dimension numerica de manera individual*/
    for(dimension <- 0 until ret.length)
    {
      ret(dimension) = caimBinning(dimension, dataMatrix, Constants.CLASS_LABELS)
    }
    return ret
  }
  
  def caimBinning(dimension:Integer, dataMatrix:DataMatrix, classLabels:ArrayBuffer[String]):ArrayBuffer[MacroBin] = 
  {
    val ret = ArrayBuffer[MacroBin]()
        
    /*Extraemos la dimension actual y la ordenamos*/
    val dataDimension = for(i <- dataMatrix.data) yield i.measures(dimension)
    val dataSorted = dataDimension.sortWith(_<_)
    
    /*Obtenemos la lista de valores unicos en los datos (distinct values),
     * la cual es la lista de posibles puntos de corte de los intervalos en
     * la discretizacion (CP = CutPoints)*/
    /*Como estan ordenados, basta con comparar el anterior con el actual.
     * No utilizamos la funcion distinct de ArrayBuffer porque es menos eficiente*/
    var currentValue = dataSorted(0)
    var remainingCPs = ArrayBuffer[Double]()
    remainingCPs += currentValue
    for (i <- 1 until dataSorted.length)
      if (dataSorted(i-1) != currentValue) 
      {
        remainingCPs += dataSorted(i-1)
        currentValue = dataSorted(i-1)
      }
    
    /*Valores posibles a analizar por el algoritmo*/
    var totalCPs = remainingCPs.length
    
    /*Extraemos el valor maximo y minimo y se lo descontamos a los CP a observar */
    val minValue = remainingCPs(0)
    val maxValue = remainingCPs(totalCPs-1)
    totalCPs -= 2
    
    var GlobalCAIM = 0.0
		var tmpCAIM = 0.0
    
		
    /* INICIALIZACION (valores iniciales antes de entrar al bucle)
		*
		*  cutPoints = {maxValue} (puntos de corte escogidos)
		*  numCPs = 1 (numero de cutPoints)
		*  remainingCPs = (posibles intervalos, en cola para analizar)
		*  numRemainingValues = size(remainingCPs)
		*  GlobalBins = {minValue-maxValue}
		*
		* */
    val cutPoints = ArrayBuffer[Double]()
    cutPoints += maxValue
    var numCPs = 1
    val globalBins = ArrayBuffer[MicroBin]()
    globalBins += new MicroBin(minValue - 1, maxValue)
    
    var candidateCP = 0.0
    var pos = 0
    var tmpBins = ArrayBuffer[MicroBin]()
    var tmpBin:MicroBin = null
    var tmpLowerBound = 0.0
    var tmpMaxCAIM = 0.0
    var tmpMaxIndex = 0
    var tmpMaxPos = 0
    var tmpNumBins = 0
    
    val numRows = dataMatrix.rows
    /*Mientras queden posibles CP en remainingCPs*/
    for(numRemainingValues <- (0 until totalCPs).reverse)
    {
      tmpMaxCAIM = -Double.MaxValue
      tmpMaxIndex = -1
      tmpMaxPos = -1
      for(i <- 0 until numRemainingValues)
      {
        candidateCP = remainingCPs(i) //Cogemos el CutPoint candidato
        //Buscamos el CutPoint superior
        pos = numCPs
        for (j <- (0 until numCPs).reverse )
        {
            
        }
      }
    }
    
    return ret
  }
  
}