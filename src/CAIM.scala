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
      if (dataSorted(i) != currentValue) 
      {
        remainingCPs += dataSorted(i)
        currentValue = dataSorted(i)
      }
    
    /*Extraemos el valor maximo y minimo y se lo descontamos a los CP a observar */
    val minValue = remainingCPs(0)
    val maxValue = remainingCPs(remainingCPs.length - 1)
    remainingCPs.remove(0)
    remainingCPs.remove(remainingCPs.length - 1)
    
    
    /* INICIALIZACION (valores iniciales antes de entrar al bucle)
		*
		*  cutPoints = {maxValue} (puntos de corte escogidos)
		*  numCPs = 1 (numero de cutPoints)
		*  remainingCPs = (posibles cutPoints, en cola para analizar)
		*  numRemainingValues = length(remainingCPs)
		*  GlobalBins = {minValue-maxValue} (bins/particiones actuales)
		*
		* */
    val cutPoints = ArrayBuffer[Double]()
    cutPoints += maxValue
    var numCPs = 1
    var numRemainingValues = remainingCPs.length
    val globalBins = ArrayBuffer[MicroBin]()
    globalBins += new MicroBin(minValue - 1, maxValue)
    
    //variables temporales de cada iteracion
    var GlobalCAIM = 0.0
		var tmpCAIM = 0.0
    var candidateCP = 0.0
    var pos = 0
    var found = false
    var tmpBins = ArrayBuffer[MicroBin]()
    var tmpBin:MicroBin = null
    var currentPoint:DataPoint = null
    var tmpLowerBound = 0.0
    var tmpMaxCAIM = 0.0
    var tmpMaxIndex = 0
    var tmpMaxPos = 0
    
    val numRows = dataMatrix.rows
    var exit = false //sustituto del break en java
    /*Mientras queden posibles CP en remainingCPs*/
    while (numRemainingValues > 0 && !exit)
    {
      tmpMaxCAIM = -Double.MaxValue
      tmpMaxIndex = -1
      tmpMaxPos = -1
      for(i <- 0 until numRemainingValues)
      {
        candidateCP = remainingCPs(i) //Cogemos el CutPoint candidato
        //Buscamos el CutPoint inmediatamente superior
        pos = numCPs -1
        found = false
        while(pos >= 0 && !found)
        {
          if (candidateCP >= cutPoints(pos))
            found = true
          else
            pos -= 1
        }
        pos += 1
        /*anadimos el CP candidato a los cutPoints, debajo del cutPoint escogido anteriormente,
         * de este modo se mantiene una estructura ordenada dentro de los cutPoints*/
        cutPoints.insert(pos,candidateCP)
        numCPs += 1
        /*Creamos las particiones posibles con los CutPoints que tenemos, anadiendo a estos el ID
         * de los puntos(datos de la BD) que se encuentran dentro de ellos*/
        tmpBins.clear()
        tmpLowerBound = minValue - 1
        for(j <- 0 until numCPs)
        {
          /*Creamos la particion*/
          tmpBin = new MicroBin(tmpLowerBound, cutPoints(j))
          /*anadimos los puntos*/
          for(r <- 0 until numRows)
          {
            currentPoint = dataMatrix.data(r)
            if(currentPoint.measures(dimension) > tmpBin.lowerBound && currentPoint.measures(dimension) <= tmpBin.upperBound)
                tmpBin.pointIDs += r;
          }
          tmpBins += tmpBin
          tmpLowerBound = cutPoints(j)
        }
        
        /*Calculamos el valor CAIM de los bins/particiones creadas anteriormente*/
        tmpCAIM = computeCAIM(tmpBins, dataMatrix, classLabels)
        if(tmpCAIM > tmpMaxCAIM) //actualizamos el maximo si es mayor
        {
          tmpMaxCAIM = tmpCAIM
          tmpMaxIndex = i
          tmpMaxPos = pos
        }
        
        cutPoints.remove(pos) //eliminamos el cutPoint usado
        numCPs -= 1
      }
      
      //anadimos el mejor cutPoint candidato a la lista
      cutPoints.insert(tmpMaxPos, remainingCPs(tmpMaxIndex))
      numCPs += 1
      remainingCPs.remove(tmpMaxIndex) //y lo borramos de los cutPoints que faltan por analizar
          
      /*Mientras se obtenga un mejor CAIM o el numero de particiones no supere el numero de clases,
       * se sigue explorando (ademas de la condicion de que sigan habiendo cutPoints por analizar*/
      if(tmpMaxCAIM > GlobalCAIM || tmpBins.length < classLabels.length)
      {
        GlobalCAIM = tmpMaxCAIM
        globalBins.clear()
        globalBins ++= tmpBins
      }
      else
        exit = true;
      numRemainingValues -= 1
    }
    val numMicroBins = globalBins.length
    var tmpMacroBin:MacroBin = null
    var tmpMicroBin:MicroBin = null
    for(i <- 0 until numMicroBins)
    {
      tmpMicroBin = globalBins(i)
      tmpMacroBin = new MacroBin(tmpMicroBin.lowerBound, tmpMicroBin.upperBound)
      ret += tmpMacroBin
    }
    
    return ret
  }
  
  def computeCAIM (bins:ArrayBuffer[MicroBin], dataMatrix:DataMatrix, classLabels:ArrayBuffer[String]):Double =
  {
    var caim = 0.0
    val numBins = bins.length
    var tmpBin:MicroBin = null
    var tmpNumPoints = 0
    var tmpPoint:DataPoint = null
    val numClasses = classLabels.length
    val classSupports = Array[Integer](numClasses)
    var exists = false
    var maxClassSupport = 0
    
    for(i <- 0 until numBins)
    {
      for(j <- 0 until numClasses)
        classSupports(j) = 0;
      
      tmpBin = bins(i)
      tmpNumPoints = tmpBin.pointIDs.length
      for(j <- 0 until tmpNumPoints)
      {
        tmpPoint = dataMatrix.data(tmpBin.pointIDs(j))
        exists = false
        var k = 0
        while(k < numClasses && !exists)
        {
          if(tmpPoint.classID == classLabels(k))
          {
            classSupports(k) += 1
            exists = true
          }
          else
            k += 1;
        }
        if (exists == false)
          throw new Exception("CAIM: Class not found!");
      }
      maxClassSupport = -1
      for(j <- 0 until numClasses)
        if(classSupports(j) > maxClassSupport)
            maxClassSupport = classSupports(j);
      caim += maxClassSupport * maxClassSupport / (1.0 * tmpNumPoints)
      
    }
    
    caim = caim / numBins
    
    return caim
  }
  
}