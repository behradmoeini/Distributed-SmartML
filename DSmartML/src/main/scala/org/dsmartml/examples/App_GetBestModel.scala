package org.dsmartml.examples

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.DoubleType
import org.dsmartml._

import scala.collection.mutable.ListBuffer

object App_GetBestModel {


  def main(args: Array[String]): Unit = {


    // Set File pathsd
    //*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
    //1- Google Cloud -->
    //-------------------------------------------------
    var dataFolderPath = "gs://sparkstorage/datasets/"
    var logpath = "/home/eissa_abdelrahman5/"

    //2- Local -->
    //-------------------------------------------------
    //var dataFolderPath = "/media/eissa/New/data/"
    //var logpath = "/media/eissa/New/data/"

    //3- Azure Cloud -->
    //-------------------------------------------------
    //var dataFolderPath = "wasb:///example/data/"
    //var logpath = "/home/sshuser/"


    // Dataset
    val i = args(0).toInt
    // type (Grid Search, TransmogrifAI, Random Search, DSmart ML)
    val j = args(1).toInt
    // Time Limit
    val t = args(2).toInt
    // Parallelism
    val p = 3 //args(3).toInt

    var TargetCol = "y"

    // Create Spark Session
    implicit val spark = SparkSession
      .builder()
      .appName("Distributed Smart ML 1.0")
      //.config("packages" , "com.salesforce.transmogrifai:transmogrifai-core_2.11:0.5.3")
      //.config("spark.master", "local")
      .config("spark.master", "yarn")
      //.config("spark.executor.memory", "15g")
      //.config("spark.driver.memory", "6g")
      //.config("spark.storage.memoryFraction" , "2")
      //.config("spark.driver.cores" , 2)
      //.config("spark.executor.cores" , 7)
      .getOrCreate()

    // Set Log Level to error only (don't show warning)
    spark.sparkContext.setLogLevel("ERROR")

    //Create Logger Instance
    var logger = new Logger(logpath)
    try{

      // Load Dataset
      var dataloader = new DataLoader(spark, i, dataFolderPath, logger)
      var rawdata = dataloader.getData()


      // get best Model for this dataset using Distributed SmartML Library
      //===================================================================================
      if (j == 1 || j == 2 ) {
        //for (ti <- Array(20,40,60,80,100) ){
        try {
          var mselector = new ModelSelector(  spark,
                                              logpath,
                                              eta = 5,
                                              maxResourcePercentage = 100,
                                              HP_MaxTime = t,
                                              HPOptimizer = j
                                            )
          var res = mselector.getBestModel(rawdata)
        }
        catch {
          case ex: Exception => println(ex.getMessage)
        }
      }

      // Grid Search
      //===================================================================================
      if (j == 3) {
        println("Grid Search")
        val starttime2 = new java.util.Date().getTime
        logger.logOutput("============= Grid Search============================================= \n")
        var grdSearchMgr: GridSearchManager = new GridSearchManager()
        var res2 = grdSearchMgr.Search(spark, rawdata)
        logger.logOutput("--Best Algorithm:" + res2._1 + " \n")
        logger.logOutput("--Accuracy:" + res2._2._3 + " \n")
        val Endtime2 = new java.util.Date().getTime
        val TotalTime2 = Endtime2 - starttime2
        logger.logOutput("--Total Time:" + (TotalTime2 / 1000.0).toString + " \n")
        if (res2._2._2 != null)
          logger.logOutput("Parameters:" + res2._2._2 + " \n")
        logger.logOutput("===================================================================== \n")
      }

    }
    catch{
      case ex:Exception => println(ex.getMessage)
        //                         logger.logOutput("Exception: " + ex.getMessage)
        logger.close()
    }
    //println("Model summary:\n" + model.summaryPretty())
    logger.close()
  }


}
