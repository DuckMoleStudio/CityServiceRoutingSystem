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

public class FuckingHell {
    public static void main(String[] args) {

        // ----- CONTROLS (set before use) -----------
        String jsonInputFile = "C:\\Users\\User\\Documents\\GD\\data\\zao-park-good.json";
        String dataDir = "C:\\Users\\User\\Documents\\GD\\data\\";

        //OSM data
        String osmFile = "C:/Users/User/Downloads/RU-MOW.osm.pbf";
        String dir = "local/graphhopper";

        boolean evaluate = false; // only evaluate, do not start algo
        boolean filterByDistrict = false; // should we apply following filter
        String[] filter = {"муниципальный округ Бибирево"};
        LocalDate date = LocalDate.of(2021, 11, 21);
        String algo = "greedy"; // algo to perform, "greedy", "jsprit"
        LocalTime startTime = LocalTime.parse("06:00");
        int noOfCars = 8;
        boolean good = true; // good -- with relevant access from right curbside
        int capacity = 80;
        int iterations = 200; // these 2 for jsprit algo

        String matrixFileJ = "C:\\Users\\User\\Documents\\GD\\data\\zao-good-wp.json";
        String matrixFileB = "C:\\Users\\User\\Documents\\GD\\data\\zao-good.bin";

        String urlOutputFile = "C:\\Users\\User\\Documents\\GD\\bbb.txt";
        String arrOutputFile = "C:\\Users\\User\\Documents\\GD\\bb.txt";
        String outDir = "C:\\Users\\User\\Documents\\GD\\tracks\\b";
        // ----- CONTROLS END ---------


        // ------- RESTORE MATRIX FROM JSON FILE ---------

        List<WayPoint> wayPointList = new ArrayList<>();
        Map<WayPoint, MatrixLineMap> matrix = new HashMap<>();
        try {
            LoadMatrix.restore(wayPointList, matrix, jsonInputFile);
        } catch (Exception e) {
            System.out.println("Invalid data file provided, available:");
            DataDir.list(dataDir);
            return;
        }





           SaveMatrixB.saveB(wayPointList,matrix,matrixFileJ,matrixFileB);

    }
}
