package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.storage.MatrixStorageLine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.graphhopper.util.StopWatch;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DataDir {
    public static void list (String dataDir) {

        try (DirectoryStream<Path> files = Files.newDirectoryStream(Path.of(dataDir))) {
            for (Path path : files)
                System.out.println(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listDistricts(String jsonInputFile) {

        StopWatch sw = new StopWatch().start();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<WayPoint> inMatrix = new ArrayList<>();
        List<String> inStrings = new ArrayList<>();

        try {
            inStrings = Files.readAllLines(Paths.get(jsonInputFile));
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        for (String ss : inStrings) {
            try {
                //inMatrix.add(objectMapper.readValue(ss, MatrixStorageLine.class));
                inMatrix.add(objectMapper.readValue(ss, WayPoint.class));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        TreeSet<String> districts = new TreeSet<>();
        for (WayPoint msl : inMatrix) {
            districts.add(msl.getDistrict());
        }

        for(String ss: districts) System.out.println(ss);
        System.out.println("loaded in: " + sw.stop().getSeconds() + " s");
    }
}
