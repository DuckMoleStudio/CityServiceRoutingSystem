package CityServiceRoutingSystem.fileManagement;

import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.inputData.XLSPaidParkings;

import com.graphhopper.util.StopWatch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XlsParkingImport {

    public  static List<WayPoint> load(String XLSInputFile1, String XLSInputFile2, String filter)
    {
        List<WayPoint> wayPointList = new ArrayList<>();

        StopWatch sw = new StopWatch().start();
        List<XLSPaidParkings> input = loadXLSPaidParkings(XLSInputFile1);
        System.out.println("\nXLS parkings loaded in: " + sw.stop().getSeconds() + " s");

        for (XLSPaidParkings pp : input)

            if (pp.getAdmArea().equals(filter))
            {
                WayPoint wp = new WayPoint();
                wp.setIndex(pp.getGlobal_id());
                wp.setDescription(pp.getAddress());
                wp.setDistrict(pp.getDistrict());
                wp.setSchedule("ex Sundays");

                String[] digits = pp.getCoordinates().split("[^[0-9.]]+"); // magic regex

                wp.setLat(Double.parseDouble(digits[2]));
                wp.setLon(Double.parseDouble(digits[1]));

                wp.setType(WayPointType.Paid_Parking);
                wp.setCapacity(1);
                wayPointList.add(wp);
            }

        System.out.println("Loaded " + wayPointList.size() + " parkings for " + filter + "\n");


        sw = new StopWatch().start();
        input = loadXLSPaidParkings(XLSInputFile2);
        System.out.println("XLS signs loaded in: " + sw.stop().getSeconds() + " s");

        for (XLSPaidParkings pp : input)

            if (pp.getAdmArea().equals(filter))
            {
                WayPoint wp = new WayPoint();
                wp.setIndex(pp.getGlobal_id());
                wp.setDescription(pp.getAddress());
                wp.setDistrict(pp.getDistrict());
                wp.setSchedule("all");

                String[] digits = pp.getCoordinates().split("[^[0-9.]]+"); // magic regex

                wp.setLat(Double.parseDouble(digits[2]));
                wp.setLon(Double.parseDouble(digits[1]));

                wp.setType(WayPointType.NoStop_Sign);
                wp.setCapacity(1);
                wayPointList.add(wp);
            }
        System.out.println("Loaded " + wayPointList.size() + " signs for " + filter + "\n");




            return wayPointList;
    }

    public static List<XLSPaidParkings> loadXLSPaidParkings(String fileName)
    { // read XLS & parse to data

        List<XLSPaidParkings> requestList = new ArrayList<>();

        // Read XLS file (Excel 97-2003)
        // Get the workbook instance for XLS file

        try(
        FileInputStream inputStream = new FileInputStream(fileName);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {
            // Get first sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(0);

            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // skip first row with headers
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Get significant fields for XLSPaidParkings class

                long id = Long.parseLong(row.getCell(0).getStringCellValue().substring(1)); // weird input
                String admArea = row.getCell(4).getStringCellValue();
                String district = row.getCell(5).getStringCellValue();
                String address = null;
                try {
                    address = row.getCell(6).getStringCellValue();
                } catch (Exception e) {
                    address = "unknown";
                }
                String coordinates = row.getCell(18).getStringCellValue();

                requestList.add(new XLSPaidParkings(id,admArea,district,address,coordinates));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (requestList);


    }
}
