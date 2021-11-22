package CityServiceRoutingSystem.toolsAdmin;

import CityServiceRoutingSystem.entity.*;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.DoubleMatrix;
import CityServiceRoutingSystem.fileManagement.SaveMatrixDouble;
import CityServiceRoutingSystem.fileManagement.SaveMatrixDoubleB;
import CityServiceRoutingSystem.fileManagement.XlsParkingImport;
import CityServiceRoutingSystem.service.*;

import CityServiceRoutingSystem.service.greedyAlgos.SimpleTSP;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class PrepareParkings {
    public static void main(String[] args) {



        // ----- CONTROLS (set before use) -----------
        String XLSInputFile1 = "C:\\Users\\User\\Documents\\GD\\park_all.xls";
        String XLSInputFile2 = "C:\\Users\\User\\Documents\\GD\\signs_all.xls";

        String filter = "Восточный административный округ"; // "Северо-Восточный административный округ", "муниципальный округ Академический"...

        //OSM data
        String osmFile = "C:/Users/User/Downloads/RU-MOW.osm.pbf";
        String dir = "local/graphhopper";

        String jsonOutputFile1 = "C:\\Users\\User\\Documents\\GD\\data\\vao-park-good.json";
        String jsonOutputFile2 = "C:\\Users\\User\\Documents\\GD\\data\\vao-park-bad.json";
        String binOutputFile1 = "C:\\Users\\User\\Documents\\GD\\data\\vao-park-good.bin";
        String binOutputFile2 = "C:\\Users\\User\\Documents\\GD\\data\\vao-park-bad.bin";

        LocalTime startTime = LocalTime.parse("06:00");
        boolean runAlgo = true; // execute itinerary routing algo at this stage?
        int noOfCars = 100;
        // ----- CONTROLS END ---------




        // ----- IMPORT FROM XLS & PARSE ------
        List<WayPoint> wayPointList = XlsParkingImport.load(XLSInputFile1,XLSInputFile2,filter);

        // ---- CREATE BASE ----


        WayPoint base = new WayPoint(0, 55.75468, 37.69016,
                "BASE", "all", "all",  // ул. Золоторожский Вал, 4А ))
                LocalTime.parse("06:00"), LocalTime.parse("08:00"), Duration.ofMinutes(1),
                WayPointType.Base, 1);


        // ------ FILL 2 MATRICES -----

        DoubleMatrix doubleMatrix = Matrix.FillGHDouble(
                wayPointList,osmFile,dir,base);


        // ----- SAVE MATRICES IN JSON -----
        SaveMatrixDoubleB.saveB(
                doubleMatrix,
                jsonOutputFile1,
                binOutputFile1,
                jsonOutputFile2,
                binOutputFile2);


        // ----- EXECUTE ITINERARY ALGORITHM IF DESIRED FOR GOOD & BAD -----
        if(runAlgo)
        {
            if(doubleMatrix.getMapGood().size()>0) {

                Result rr = SimpleTSP.Calculate(
                        doubleMatrix.getWayPointsGood(),
                        doubleMatrix.getMapGood(),
                        noOfCars,
                        startTime);
            }

            if(doubleMatrix.getMapBad().size()>0) {

                Result rr = SimpleTSP.Calculate(
                        doubleMatrix.getWayPointsBad(),
                        doubleMatrix.getMapBad(),
                        noOfCars,
                        startTime);
            }
        }
    }
}
