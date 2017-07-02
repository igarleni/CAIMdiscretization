

object Constants 
{
  var FILE_INPUT: String = null
  var FILE_CP_OUTPUT: String = null
  var FILE_RUNTIME_OUTPUT: String = null
  var FILE_DATA_OUTPUT: String = null
  var FILE_ORG_INPUT: String = null
  var FILE_ORG_NORMAL_INPUT: String = null
  var FILE_IDIST: String = null
  
  var NUM_ROWS = 0
  var NUM_MEASURE_COLS = 0
  var NUM_CAT_CONTEXT_COLS = 0
  var NUM_ORG_CAT_CONTEXT_COLS = 0
  
  var FIELD_DELIMITER = ";"
  
  var MAX_VAL:Double = 0.0
  
  var METHOD:String = null
  val MB_MDL_DP = 0	  // MDL dynamic programming
	val MB_MDL_GD = 1  	// MDL greedy
	val MB_MDL_EGD = 2	// normal greedy
	val EW = 3		  	  // equal-width
	val EF = 4    			// equal-frequency
	val CAIM = 5	    	// equal-frequency
	val MB_NM_GD = 6  	// normal greedy
	val J_MDL = 9    		// normal greedy
	val J_MDL_HM = 10  	// normal greedy
	val J_GD_HM = 11  	// normal greedy
	val M_GD_CAIM = 7  	// normal greedy
	val M_GD = 8		    // normal greedy
	val MMIC = 12		    // normal greedy
	val FayyadMDL = 14	// normal greedy
	val KoMDL = 15	  	// normal greedy
	val DP_MEAN = 16  	// MDL dynamic programming
	val DP_MIN = 17    	// MDL dynamic programming
	val DP_MAX = 18	    // MDL dynamic programming
	val MVD = 19	      // MDL dynamic programming
	val PCABINNING = 20
	
}