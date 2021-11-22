package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.inputData.GarbageSiteMD;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JsonGarbageImport {
    public static List<WayPoint> load(String filename, String filter)
    {
        List<GarbageSiteMD> input = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String inString = null;
        try {
            inString = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            input = objectMapper.readValue(inString, new TypeReference<List<GarbageSiteMD>>(){});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Criteria : " + filter);

        // ----- PARSE DATA FROM MOSDATA JSON & FILTER ------

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
        System.out.println("\nLoaded " + wayPointList.size() + " sites\n");
        return wayPointList;
    }
}
