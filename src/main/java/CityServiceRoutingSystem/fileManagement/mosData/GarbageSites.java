package CityServiceRoutingSystem.fileManagement.mosData;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.inputData.GarbageSiteMD;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphhopper.util.StopWatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GarbageSites {

    public static List<WayPoint> load(String jsonInputFile, String filter)
    {

        // ----- IMPORT FROM MOSDATA JSON & FILTER ------
        StopWatch sw = new StopWatch().start();
        List<GarbageSiteMD> input = read(jsonInputFile);
        System.out.println("\nMosData garbage sites loaded in: " + sw.stop().getSeconds() + " s");

        List<WayPoint> wayPointList = new ArrayList<>();
        for (GarbageSiteMD gs : input) {
            if (gs.getYardLocation()[0].getAdmArea().equals(filter)) {
                WayPoint wp = new WayPoint();
                wp.setIndex(gs.getGlobal_id());
                wp.setLat(gs.getGeoData().getCoordinates()[1]);
                wp.setLon(gs.getGeoData().getCoordinates()[0]);
                wp.setDescription(gs.getYardName());
                wp.setDistrict(gs.getYardLocation()[0].getDistrict());
                wp.setDuration(Duration.ofMinutes(10));
                wp.setType(WayPointType.Garbage_Site);
                wp.setCapacity(1);

                wayPointList.add(wp);
            }
        }

        System.out.println("Loaded " + wayPointList.size() + " garbage sites for " + filter + "\n");

        return wayPointList;
    }

    public static List<GarbageSiteMD> read(String filename)
    {

        List<GarbageSiteMD> input = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String inString = null;
        try {
            inString = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(inString);
        try {
            input = objectMapper.readValue(inString, new TypeReference<List<GarbageSiteMD>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return input;
    }
}
