import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd._
import org.apache.spark.SparkConf

object MainClass {
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
      //readInputString(args)
      println("----------------App end----------------")
    }
    catch
    {
      case ex:Exception => ex.printStackTrace
    }
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
    if (found == false)
			throw new Exception("Missing -FILE_IDIST");
    
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
      if (args(i).equals("-NUM_ROWS"))
      {
        Constants.NUM_ROWS = args(i+1).toInt
        found = true
      }
    }
    if (found == false)
			throw new Exception("Missing -NUM_ROWS");
    
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
    if (found == false)
			throw new Exception("Missing -FIELD_DELIMITER");
    
    found = false
    for (i <- 0 until total if !found)
    {
      if (args(i).equals("-MAX_VAL"))
      {
        Constants.MAX_VAL = args(i+1).toInt
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