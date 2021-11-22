package CityServiceRoutingSystem.toolsAdmin;

import CityServiceRoutingSystem.RandomData.DistType;
import CityServiceRoutingSystem.RandomData.MockTimeSlots;
import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.fileManagement.SaveMatrix;
import CityServiceRoutingSystem.fileManagement.SaveMatrixB;
import CityServiceRoutingSystem.fileManagement.mosData.GarbageSites;
import CityServiceRoutingSystem.service.Matrix;
import CityServiceRoutingSystem.service.greedyAlgos.TimeslotVRP;

import java.time.Duration;
import java.time.LocalTime;

import java.util.List;
import java.util.Map;


public class PrepareGarbage {
    public static void main(String[] args) {
        // ----- CONTROLS (set before use) -----------
        String jsonInputFile = "C:\\Users\\User\\Documents\\GD\\MDjson-ALL.json";
        String filter = "Южный административный округ"; // "Северо-Восточный административный округ", ...

        // random timeslot generation params
        int timeStartMin = 6; // opening time, hours in 24h
        int timeStartMax = 13; // non-inclusive
        DistType timeDist = DistType.Descend; // Statistic distribution, Equal, Gaussian, asc&desc Expo
        int intervalMin = 1; // availability from open to close, hours
        int intervalMax = 8; // non-inclusive
        DistType intervalDist = DistType.Ascend;
        int maxCapacity=5; // for random capacity 1 to this inclusive

        //OSM data
        String osmFile = "C:/Users/User/Downloads/RU-MOW.osm.pbf";
        String dir = "local/graphhopper";

        String jsonOutputFile = "C:\\Users\\User\\Documents\\GD\\data\\yao-garbage.json";
        String binOutputFile = "C:\\Users\\User\\Documents\\GD\\data\\yao-garbage.bin";

        LocalTime startTime = LocalTime.parse("06:00");
        boolean runAlgo = false; // execute itinerary routing algo at this stage?
        int capacity = 50; // garbage car capacity in abstract units
        // ----- CONTROLS END ---------


        // ----- IMPORT FROM JSON & PARSE ------
        List<WayPoint> wayPointList = GarbageSites.load(jsonInputFile,filter);


        // ---- NOW INIT RANDOM TIMESLOTS while we haven't got real ones, schedule too -----
        MockTimeSlots.fill(
                wayPointList,
                timeStartMin,
                timeStartMax,
                timeDist,
                intervalMin,
                intervalMax,
                intervalDist,
                maxCapacity
        );

        // ---- ADD BASE & DUMP ----
        wayPointList.add(new WayPoint(0, 55.766, 37.532, "BASE",
                "all", "all",  //MKM-logistika
                LocalTime.parse("06:00"), LocalTime.parse("22:00"), Duration.ofMinutes(1),
                WayPointType.Base, 1));
        wayPointList.add(new WayPoint(1, 55.769, 37.5, "DUMP SITE",
                "all", "all", // 55.769 37.5 Силикатный  55.376 39 Egoryevsk
                LocalTime.parse("06:00"), LocalTime.parse("22:00"),
                Duration.ofMinutes(1),
                WayPointType.Garbage_Dump, Integer.MAX_VALUE));



        // ------ NOW FILL THE MATRIX -----

        Map<WayPoint, MatrixLineMap> matrix = Matrix.FillGHMulti4Map(
                wayPointList, osmFile, dir, false, false);


        // ----- AND SAVE THE MATRIX IN JSON -----

        //SaveMatrix.save(wayPointList, matrix, jsonOutputFile);
        SaveMatrixB.saveB(wayPointList,matrix,jsonOutputFile,binOutputFile);


        // ----- EXECUTE ITINERARY ALGORITHM IF DESIRED -----
        if(runAlgo)
        {
            Result rr = TimeslotVRP.Calculate(wayPointList, matrix, capacity, startTime);
        }
    }
}
