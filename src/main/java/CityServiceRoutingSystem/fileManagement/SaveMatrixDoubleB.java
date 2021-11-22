package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.storage.DoubleMatrix;

public class SaveMatrixDoubleB {
    public static void saveB(
            DoubleMatrix doubleMatrix,
            String jsonOutputFile1,
            String binOutputFile1,
            String jsonOutputFile2,
            String binOutputFile2)
    {


        if(doubleMatrix.getMapGood().size()>0)
        {
            SaveMatrixB.saveB(doubleMatrix.getWayPointsGood(),
                    doubleMatrix.getMapGood(),
                    jsonOutputFile1,
                    binOutputFile1);
        }

        if(doubleMatrix.getMapBad().size()>0)
        {
            SaveMatrixB.saveB(doubleMatrix.getWayPointsBad(),
                    doubleMatrix.getMapBad(),
                    jsonOutputFile2,
                    binOutputFile2);
        }
    }
}
