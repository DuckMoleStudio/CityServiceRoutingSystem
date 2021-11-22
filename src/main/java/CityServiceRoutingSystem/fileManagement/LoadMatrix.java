package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.entity.storage.MatrixStorageLine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.graphhopper.util.StopWatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class LoadMatrix {
    public static void restore(List<WayPoint> wayPointList,
                               Map<WayPoint, MatrixLineMap> matrix,
                               String jsonInputFile) throws Exception
    {
        StopWatch sw = new StopWatch().start();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<MatrixStorageLine> inMatrix = new ArrayList<>();

        List<String> inStrings = new ArrayList<>();

        /*

        try {
            inStrings = Files.readAllLines(Paths.get(jsonInputFile));
        } catch (IOException e)
        {
           throw e;
        }

         */

        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(jsonInputFile));
        } catch (IOException e) {
            throw e;
        }
        try {
            while (it.hasNext()) {
               inStrings.add(it.nextLine());
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        for (String ss : inStrings) {
            try {
                inMatrix.add(objectMapper.readValue(ss, MatrixStorageLine.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        for (MatrixStorageLine msl : inMatrix) {
            wayPointList.add(msl.getWayPoint());
        }

        for (MatrixStorageLine msl : inMatrix)
        {
            MatrixLineMap ml = new MatrixLineMap();
            ml.setDistances(new HashMap<>());
            for (int i = 0; i < msl.getDistances().size(); i++)
            {
                ml.getDistances().put(wayPointList.get(i), msl.getDistances().get(i));
            }
            matrix.put(msl.getWayPoint(),ml);

        }
        System.out.println("\nRestored matrix from: " +
                jsonInputFile +
                " " +
                wayPointList.size() +
                " waypoints");

        System.out.println("loaded in: " + sw.stop().getSeconds() + " s");
    }
}
