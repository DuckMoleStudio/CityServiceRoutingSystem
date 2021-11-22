package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.result.Itinerary;
import CityServiceRoutingSystem.entity.result.Result;

import java.io.FileWriter;
import java.io.IOException;

public class WriteArrivals {
    public static void write(Result rr, String txtOutputFile)
    {
        try (FileWriter writer = new FileWriter(txtOutputFile))
        {
            for (Itinerary ii : rr.getItineraries())
            {
                String itinerary = ii.getCar().getDescription() + "\n";
                for(int i=0; i<ii.getWayPointList().size(); i++)
                {

                    itinerary+=i + ": " + ii.getWayPointList().get(i).getDescription() +
                            " arrival at " + ii.getArrivals().get(i) +  " (open " +
                            ii.getWayPointList().get(i).getTimeOpen() +" - " +
                            ii.getWayPointList().get(i).getTimeClose() + ") on: " +
                    ii.getWayPointList().get(i).getSchedule() + "\n";
                }

                writer.write(itinerary);
                writer.write("\n\n");
            }
            writer.flush();
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        System.out.println("\nSaved as: " + txtOutputFile);
    }
}
