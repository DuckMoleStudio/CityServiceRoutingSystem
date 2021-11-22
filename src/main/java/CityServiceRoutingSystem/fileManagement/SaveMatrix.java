package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.entity.storage.MatrixStorageLine;
import CityServiceRoutingSystem.entity.storage.TimeDistancePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaveMatrix {
    public static void save(
            List<WayPoint> wayPointList,
            Map<WayPoint, MatrixLineMap> matrix,
            String fileName)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());



        try (FileWriter writer = new FileWriter(fileName)) {
            for (WayPoint wp : wayPointList)
            {
                MatrixStorageLine msl = new MatrixStorageLine();
                msl.setWayPoint(wp);

                List<TimeDistancePair> dd = new ArrayList<>();
                for(WayPoint wwp : wayPointList)
                {
                    dd.add(matrix.get(wp).getDistances().get(wwp));
                }

                msl.setDistances(dd);

                writer.write(objectMapper.writeValueAsString(msl));
                writer.write("\n");
            }
            writer.flush();
            System.out.println("Saved as: " + fileName);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }


    }
}
