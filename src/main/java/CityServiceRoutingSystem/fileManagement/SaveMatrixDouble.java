package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.storage.DoubleMatrix;
import CityServiceRoutingSystem.entity.storage.MatrixStorageLine;
import CityServiceRoutingSystem.entity.storage.TimeDistancePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveMatrixDouble {
    public static void save(
                            DoubleMatrix doubleMatrix,
                            String jsonOutputFile1,
                            String jsonOutputFile2)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        if(doubleMatrix.getMapGood().size()>0) {
            try (FileWriter writer = new FileWriter(jsonOutputFile1)) {
                for (WayPoint wp : doubleMatrix.getWayPointsGood()) {
                    MatrixStorageLine msl = new MatrixStorageLine();
                    msl.setWayPoint(wp);

                    List<TimeDistancePair> dd = new ArrayList<>();
                    for (WayPoint wwp : doubleMatrix.getWayPointsGood()) {
                        dd.add(doubleMatrix.getMapGood().get(wp).getDistances().get(wwp));
                    }

                    msl.setDistances(dd);

                    writer.write(objectMapper.writeValueAsString(msl));
                    writer.write("\n");
                }
                writer.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("Saved as: " + jsonOutputFile1);
        }

        if(doubleMatrix.getMapBad().size()>0) {
            try (FileWriter writer = new FileWriter(jsonOutputFile2)) {
                for (WayPoint wp : doubleMatrix.getWayPointsBad()) {
                    MatrixStorageLine msl = new MatrixStorageLine();
                    msl.setWayPoint(wp);

                    List<TimeDistancePair> dd = new ArrayList<>();
                    for (WayPoint wwp : doubleMatrix.getWayPointsBad()) {
                        dd.add(doubleMatrix.getMapBad().get(wp).getDistances().get(wwp));
                    }

                    msl.setDistances(dd);

                    writer.write(objectMapper.writeValueAsString(msl));
                    writer.write("\n");
                }
                writer.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("Saved as: " + jsonOutputFile2 + "\n");
        }
    }
}
