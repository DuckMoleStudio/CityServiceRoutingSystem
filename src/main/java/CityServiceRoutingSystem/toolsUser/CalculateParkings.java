package CityServiceRoutingSystem.toolsUser;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.fileManagement.*;
import CityServiceRoutingSystem.service.Schedule;
import CityServiceRoutingSystem.service.greedyAlgos.SimpleTSP;
import CityServiceRoutingSystem.service.greedyAlgos.TimeslotVRP;
import CityServiceRoutingSystem.service.jspritAlgos.JspritShipmentVRPwTimeslots;
import CityServiceRoutingSystem.service.jspritAlgos.JspritTSP;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class CalculateParkings {
    public static void main(String[] args) {

        // ----- CONTROLS (set before use) -----------
        String jsonInputFile = "C:\\Users\\User\\Documents\\GD\\data\\zao-garbage.json";
        String binInputFile = "C:\\Users\\User\\Documents\\GD\\data\\zao-garbage.bin";
        String dataDir = "C:\\Users\\User\\Documents\\GD\\data\\";

        //OSM data
        String osmFile = "C:/Users/User/Downloads/RU-MOW.osm.pbf";
        String dir = "local/graphhopper";

        boolean evaluate = false; // only evaluate, do not start algo
        boolean filterByDistrict = true; // should we apply following filter
        String[] filter = {"район Раменки", "район Проспект Вернадского"};
        LocalDate date = LocalDate.of(2021, 11, 23);
        String algo = "greedyGarbage"; // algo to perform, "greedyPapkings", "jspritParkings"
        LocalTime workStart = LocalTime.parse("06:00");
        LocalTime workEnd = LocalTime.parse("22:00");
        boolean isGood = false; // with access from R curbside, for GPX. True for "good" files, false for rest
        int capacity = 50;
        int iterations = 200; // these 2 for jsprit algo

        String urlOutputFile = "C:\\Users\\User\\Documents\\GD\\a1.txt";
        String arrOutputFile = "C:\\Users\\User\\Documents\\GD\\a2.txt";
        String outDir = "C:\\Users\\User\\Documents\\GD\\tracks\\a3";
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

            System.out.printf("Greedy Algo will perform in approx. %d seconds\n",
                    filteredWayPointList.size()/600+1);
            System.out.printf("Jsprit Algo will perform in approx. %d minutes in %d iterations\n",
                    (int)((Math.pow(filteredWayPointList.size(),3)/10000000)+
                            iterations*filteredWayPointList.size()/2000),iterations);
            System.out.printf("and use %d cars for capacity of %d\n",
                    filteredWayPointList.size()/capacity+1, capacity);

        } else
        {
            System.out.println("\nFilter applied, " + filteredWayPointList.size() + " waypoints left");

            // ------- RUN ALGO IF ALLOWED -------

            Result rr = new Result();
            switch(algo)
            {
                case "greedyParkings": rr = SimpleTSP.Calculate(filteredWayPointList, matrix, capacity, workStart); break;
                case "jspritParkings": rr = JspritTSP.Calculate(filteredWayPointList, matrix, capacity, iterations, workStart); break;
                case "greedyGarbage": rr = TimeslotVRP.Calculate(filteredWayPointList, matrix, capacity, workStart); break;
                case "jspritGarbage": rr = JspritShipmentVRPwTimeslots.Calculate(filteredWayPointList, matrix, capacity, iterations, workStart,workEnd); break;
            }


            // ----- SAVE RESULTS FOR VISUALISATION ------

            WriteGH.write(rr, urlOutputFile);
            WriteGPX.write(osmFile, dir, outDir, rr, isGood);
            WriteArrivals.write(rr, arrOutputFile);
        }
    }
}
