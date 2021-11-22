package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.result.Itinerary;
import CityServiceRoutingSystem.entity.result.Result;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.gpx.GpxConversions;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WriteGPX {
    public static void write(String osmFile,
                             String dir,
                             String outDir,
                             Result rr,
                             boolean good)
    {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation(dir);

        hopper.setProfiles(
                new Profile("car1").setVehicle("car").setWeighting("shortest").setTurnCosts(false),
                new Profile("car2").setVehicle("car").setWeighting("fastest").setTurnCosts(true).putHint("u_turn_costs", 60)
        );
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car1"), new CHProfile("car2"));
        hopper.importOrLoad();

        int j = 0;
        for (Itinerary ii : rr.getItineraries()) {
            String GPXFileName = outDir + "\\car-0" + (j++) + ".gpx";

            GHRequest req = new GHRequest().setAlgorithm(Parameters.Algorithms.ASTAR_BI);

            if (good) {
                req.setProfile("car2");
            } else {
                req.setProfile("car1");
            }

            List<String> curbSides = new ArrayList<>();
            for (WayPoint wp : ii.getWayPointList()) {
                req.addPoint(new GHPoint(wp.getLat(), wp.getLon()));
                curbSides.add("right");
            }

            if (good) req.setCurbsides(curbSides);


            GHResponse res = hopper.route(req);

            if (res.hasErrors()) {
                throw new RuntimeException(res.getErrors().toString());
            }

            String trackName = "Car # " + (j);

            String gpx = GpxConversions.createGPX(
                    res.getBest().getInstructions(),
                    trackName,
                    0,
                    false,
                    false,
                    true,
                    true,
                    "version 1.0",
                    res.getBest().getInstructions().getTr());

            System.out.println("Saving " + GPXFileName + "...");

            // check directory & create if does not exist
            try {
                Files.createDirectories(Path.of(outDir));
            } catch (IOException e) {
                e.printStackTrace();
            }


            try (FileWriter writer = new FileWriter(GPXFileName)) {

                writer.write(gpx);
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
