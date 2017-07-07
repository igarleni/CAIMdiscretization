import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd._
import org.apache.spark.SparkConf

import scala.collection.mutable.ArrayBuffer

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.File

object MainClass
{
  def main(args:Array[String]): Unit = 
  {
    val conf = new SparkConf()
    conf.set("spark.cores.max", "20")
    conf.set("spark.executor.memory", "6g")
    conf.set("spark.kryoserializer.buffer.max", "512")
    conf.setAppName("IPDdiscretization")
    val sc = new SparkContext(conf)
    
    println("----------------App init----------------")
    try
    {
      /*LECTURA DE DATOS Y CALCULO DE INFORMACION BASICA*/
      
      readInputString(args)
      for(i <- 0 until args.length) println("arg " + i +" -> " + args(i))
      
      val dataMatrix = new DataMatrix(Constants.NUM_MEASURE_COLS, Constants.NUM_CAT_CONTEXT_COLS)
      /*singleContexts = todos los posibles valores unicos de las dimensiones categoricas*/
      val singleContexts = new Array[ArrayBuffer[String]](Constants.NUM_CAT_CONTEXT_COLS)
      for(i <- 0 until Constants.NUM_CAT_CONTEXT_COLS) singleContexts(i) = new ArrayBuffer[String]()
      val writerCP = new BufferedWriter(new FileWriter(new File(Constants.FILE_CP_OUTPUT)))
				
      println()
      println("Start reading input data...")
      //val inFile = sc.textFile(Constants.FILE_INPUT)
      DataReader.readData(Constants.FILE_INPUT, dataMatrix, singleContexts);
      println("End reading input data.")
      
      
      /*CALCULO DE LA DISCRETIZACION*/
      println("Start discretization...")
      
      val finalBins:Array[ArrayBuffer[MacroBin]] = CAIM.discretizeData(dataMatrix)
      
      /*Recopilacion de resultados y escritura en ficheros*/
      
      val numCols = dataMatrix.cols
      val numCatCols = dataMatrix.catCols
      var tmpNumMacroBins = 0
      var tmpMacroBin:MacroBin = null
      var tmpPrevMacroBin:MacroBin = null
      var maxCol = 0.0
      var minCol = 0.0
      val outputBins = new Array[ArrayBuffer[MacroBin]](numCols)
      
      /*Para cada dimension...*/
      for(dimension <- 0  until numCols)
      {
        outputBins(dimension) = ArrayBuffer[MacroBin]()
        maxCol = Constants.MAX_COLS(dimension)
        minCol = Constants.MIN_COLS(dimension)
        tmpNumMacroBins = finalBins(dimension).length
        tmpPrevMacroBin = finalBins(dimension)(0)
        outputBins(dimension) += tmpPrevMacroBin
        /*guardamos las bins/particiones concatenadas (sin solapamiento)*/
        for(binIndex <- 1 until tmpNumMacroBins)
        {
          tmpMacroBin = finalBins(dimension)(binIndex)
          if (tmpMacroBin.upperBound > tmpPrevMacroBin.upperBound)
            outputBins(dimension) += tmpMacroBin;
          tmpPrevMacroBin = tmpMacroBin
        }
        tmpNumMacroBins = outputBins(dimension).length
        
        writerCP.write("dimension " + Integer.toString(dimension) + " (" + tmpNumMacroBins + " bins)")
        writerCP.newLine
        for(binIndex <- 0 until tmpNumMacroBins)
        {
          tmpMacroBin = outputBins(dimension)(binIndex)
          writerCP.write(getOriginalValue(tmpMacroBin.upperBound, minCol, maxCol).toString)
				  writerCP.newLine()
        }
        writerCP.write("-------------------------------------")
        writerCP.newLine()
      }
      println("End discretization...")
      
      writerCP.flush
      writerCP.close
      
      
      /*CONVERSION Y ESCRITURA DE LOS DATOS DISCRETIZADOS*/
      
      val numRows = Constants.NUM_ROWS
      var tmpLowerBound = 0.0
      var tmpUpperBound = 0.0
      val outputData = Array.ofDim[String](numRows,numCols)
      val pointLabels = new Array[String](numRows)
      var tmpPoint:DataPoint = null
      var prefix:String = null
      var attName:String = null
      var nominalIndex = 1
      
      
      /*1.- CABECERA*/
      
      val writerData = new BufferedWriter(new FileWriter(new File(Constants.FILE_DATA_OUTPUT)))
      writerData.write("@relation DB")
      writerData.newLine()
      writerData.newLine()
      
      /*Escribimos la informacion de los labels obtenidos para las variables numericas*/
      
      for(dimension <- 0 until numCols)
      {
        prefix = dimension.toString
        writerData.write("@attribute dim" + prefix + " {")
        tmpNumMacroBins = outputBins(dimension).length
        for(binIndex <- 0 until tmpNumMacroBins)
        {
          tmpMacroBin = outputBins(dimension)(binIndex)
          attName = nominalIndex.toString
          tmpMacroBin.name = attName
          if(binIndex == 0)
            writerData.write(attName);
          else
            writerData.write("," + attName);
          nominalIndex += 1
        }
        writerData.write("}")
        writerData.newLine
      }
      
      /*Lo mismo que antes pero con las variables categoricas*/
      for(dimension <- 0 until numCatCols)
      {
        prefix = (numCols + dimension).toString
        writerData.write("@attribute dim" + prefix + " {")
        tmpNumMacroBins = singleContexts(dimension).length
        for(index <- 0 until tmpNumMacroBins)
        {
          attName = singleContexts(dimension)(index).toString
          if(index == 0)
            writerData.write(attName);
          else
            writerData.write("," + attName);
        }
        writerData.write("}")
        writerData.newLine
      }
      
      /*Convertimos los valores a los labels obtenidos en la discretizacion*/
      var attNameExists = false
      for(i <- 0 until numRows)
      {
        tmpPoint = dataMatrix.data(i)
        pointLabels(i) = new String(tmpPoint.classID)
        for(dimension <- 0 until numCols)
        {
          attNameExists = false
          tmpNumMacroBins = outputBins(dimension).length
          var binIndex = 0
          while(binIndex < tmpNumMacroBins && !attNameExists)
          {
            tmpMacroBin = outputBins(dimension)(binIndex)
            tmpLowerBound = tmpMacroBin.lowerBound
            tmpUpperBound = tmpMacroBin.upperBound
            if(tmpPoint.measures(dimension) <= tmpUpperBound && tmpPoint.measures(dimension) >= tmpLowerBound)
            {
              outputData(i)(dimension) = tmpMacroBin.name
              attNameExists = true
            }
            else
              binIndex += 1;
          }
          if (attNameExists == false)
          {
            println(i + " --- " + dimension + " --- " + tmpPoint.measures(dimension))
            throw new Exception("Attribute nominal value not found")
          }
        }
      }
      
      /*Escribimos los labels de la variable clase*/
      writerData.write("@attribute class {")
      var numLabels = Constants.CLASS_LABELS.length
      for(i <- 0 until numLabels)
      {
        if (i ==0)
          writerData.write("\"" + Constants.CLASS_LABELS(i) + "\"");
        else
          writerData.write("," + "\"" +  Constants.CLASS_LABELS(i) + "\"");
      }
      writerData.write("}")
      writerData.newLine
      writerData.newLine
      
      
      /*2.- CUERPO/DATOS*/
      
      /*Escribimos los datos transformados a sus labels correspondientes*/
      writerData.write("@data")
      writerData.newLine
      for(i <- 0 until numRows)
      {
        tmpPoint = dataMatrix.data(i)
        for(j <- 0 until numCols)
        {
          if(outputData(i)(j) == null)
            throw new Exception("Null value");
          
          writerData.write(outputData(i)(j) + ",")
        }
        for(j <- 9 until numCatCols)
          writerData.write(tmpPoint.catContexts(j) + ",");
        writerData.write("\"" + pointLabels(i) + "\"")
        
        if(i != numRows -1)
          writerData.newLine;
      }
      
      writerData.flush
      writerData.close
    }
    catch
    {
      case ex:Exception => ex.printStackTrace
    }
  }
  
  def getOriginalValue(value:Double, min:Double, max:Double): Double =
  {
    var ret = min
    if(max > min)
      ret = (max - min) * (value + Constants.MAX_VAL) / (2 * Constants.MAX_VAL) + min;
    return ret
  }
  
  def readInputString(args:Array[String]): Unit = 
  {
    val total = args.length -1
    
    var found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FILE_INPUT"))
      {
        Constants.FILE_INPUT = args(i+1)
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -FILE_INPUT");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FILE_IDIST"))
      {
        Constants.FILE_IDIST = args(i+1)
        found = true
      }
    }
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FILE_CP_OUTPUT"))
      {
        Constants.FILE_CP_OUTPUT = args(i+1)
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -FILE_CP_OUTPUT");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FILE_RUNTIME_OUTPUT"))
      {
        Constants.FILE_RUNTIME_OUTPUT = args(i+1)
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -FILE_RUNTIME_OUTPUT");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FILE_DATA_OUTPUT"))
      {
        Constants.FILE_DATA_OUTPUT = args(i+1)
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -FILE_DATA_OUTPUT");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-NUM_MEASURE_COLS"))
      {
        Constants.NUM_MEASURE_COLS = args(i+1).toInt
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -NUM_MEASURE_COLS");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-NUM_CAT_CONTEXT_COLS"))
      {
        Constants.NUM_CAT_CONTEXT_COLS = args(i+1).toInt
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -NUM_CAT_CONTEXT_COLS");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-FIELD_DELIMITER"))
      {
        Constants.FIELD_DELIMITER = args(i+1)
        found = true
      }
    }
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-MAX_VAL"))
      {
        Constants.MAX_VAL = args(i+1).toDouble
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -MAX_VAL");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-METHOD"))
      {
        Constants.METHOD = args(i+1)
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -METHOD");
  }
  
}