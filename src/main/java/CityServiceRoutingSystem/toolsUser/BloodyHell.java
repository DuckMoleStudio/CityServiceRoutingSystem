package CityServiceRoutingSystem.toolsUser;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.fileManagement.*;
import CityServiceRoutingSystem.service.Schedule;
import CityServiceRoutingSystem.service.greedyAlgos.SimpleTSP;
import CityServiceRoutingSystem.service.jspritAlgos.JspritTSP;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodyHell {
    public static void main(String[] args) {

        // ----- CONTROLS (set before use) -----------
        String jsonInputFile = "C:\\Users\\User\\Documents\\GD\\data\\szao-park-good.json";
        String binInputFile = "C:\\Users\\User\\Documents\\GD\\data\\szao-park-good.bin";
        String dataDir = "C:\\Users\\User\\Documents\\GD\\data\\";

        //OSM data
        String osmFile = "C:/Users/User/Downloads/RU-MOW.osm.pbf";
        String dir = "local/graphhopper";

        boolean evaluate = false; // only evaluate, do not start algo
        boolean filterByDistrict = true; // should we apply following filter
        String[] filter = {"муниципальный округ Куркино"};
        LocalDate date = LocalDate.of(2021, 11, 21);
        String algo = "greedy"; // algo to perform, "greedy", "jsprit"
        LocalTime startTime = LocalTime.parse("06:00");
        int noOfCars = 3;
        boolean good = true; // good -- with relevant access from right curbside
        int capacity = 30;
        int iterations = 200; // these 2 for jsprit algo

        String urlOutputFile = "C:\\Users\\User\\Documents\\GD\\aaak.txt";
        String arrOutputFile = "C:\\Users\\User\\Documents\\GD\\aak.txt";
        String outDir = "C:\\Users\\User\\Documents\\GD\\tracks\\ak";
        // ----- CONTROLS END ---------


        // ------- RESTORE MATRIX FROM JSON FILE ---------

        List<WayPoint> wayPointList = new ArrayList<>();
        Map<WayPoint, MatrixLineMap> matrix = new HashMap<>();
        try {
            LoadMatrixB.restoreB(wayPointList, matrix, jsonInputFile,binInputFile);
        } catch (Exception e) {
            System.out.println("Invalid data file provided, available:");
            DataDir.list(dataDir);
            return;
        }


        // ------- APPLY FILTERS ------
        List<WayPoint> filteredWayPointList = new ArrayList<>();
        for (WayPoint wp : wayPointList) {
            if (Schedule.isActive(wp.getSchedule(), date)) {
                if (wp.getDistrict().equals("all")||!filterByDistrict)
                    filteredWayPointList.add(wp);
                else
                    for (String ss : filter) {
                        if (wp.getDistrict().equals(ss))
                            filteredWayPointList.add(wp);

                    }
            }
        }

        if(filteredWayPointList.size()<3)
        {
            System.out.println("Your filter gave empty selection, available districts:\n");
            DataDir.listDistricts(jsonInputFile);
            return;
        }

        // ------ EVALUATE IF NEEDED -------

        if (evaluate)
        {
            System.out.println("\nYour selected " + filteredWayPointList.size() + " waypoints");
            System.out.println("Date is " + date + " " + date.getDayOfWeek());
            System.out.printf("With %d cars you'll get %d waypoints per route\n",
                    noOfCars, filteredWayPointList.size()/noOfCars);
            System.out.printf("Greedy Algo will perform in approx. %d seconds\n",
                    filteredWayPointList.size()/600+1);
            System.out.printf("Jsprit Algo will perform in approx. %d minutes in %d iterations\n",
                    (int)((Math.pow(filteredWayPointList.size(),2)/250000)+
                            iterations*filteredWayPointList.size()/12000),iterations);
            System.out.printf("and use %d cars for capacity of %d\n",
                    filteredWayPointList.size()/capacity+1, capacity);

        } else
        {
            System.out.println("\nFilter applied, " + filteredWayPointList.size() + " waypoints left");

            // ------- RUN ALGO IF ALLOWED -------

            Result rr = new Result();
            switch(algo)
            {
                case "greedy": rr = SimpleTSP.Calculate(filteredWayPointList, matrix, noOfCars, startTime); break;
                case "jsprit": rr = JspritTSP.Calculate(filteredWayPointList, matrix, capacity, iterations, startTime); break;
            }


            // ----- SAVE RESULTS FOR VISUALISATION ------

            WriteGH.write(rr, urlOutputFile);
            WriteGPX.write(osmFile, dir, outDir, rr, good);
            WriteArrivals.write(rr, arrOutputFile);
        }
    }
}
