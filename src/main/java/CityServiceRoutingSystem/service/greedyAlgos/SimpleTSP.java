package CityServiceRoutingSystem.service.greedyAlgos;

import CityServiceRoutingSystem.entity.Car;
import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.entity.result.Itinerary;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixElement;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.service.Matrix;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class SimpleTSP {
    public static Result Calculate(
            List<WayPoint> wayPoints,
            Map<WayPoint, MatrixLineMap> matrix,
            int capacity,
            LocalTime startTime)
    {
        long elTime = System.currentTimeMillis();
        Result result = new Result();
        result.setMethodUsed("Simple Greedy Algorithm, just TSP with no. of routes V.01");
        List<Itinerary> ii = new ArrayList<>();
        result.setItineraries(ii);

        WayPoint start = new WayPoint();

        // get start
        for(WayPoint ww: wayPoints)
        {
            if (ww.getType() == WayPointType.Base) {
                start = ww;
            }
        }
        wayPoints.remove(start);

        int itCount = 0;

        while(!wayPoints.isEmpty())
        {
            Itinerary itinerary = new Itinerary(); // new car & new itinerary start here
            itinerary.setCar(new Car("Rita # " + (++itCount), WayPointType.Paid_Parking, 0));
            WayPoint curWP = start;
            List<WayPoint> ll = new ArrayList<>();
            ll.add(curWP);
            itinerary.setWayPointList(ll);


            itinerary.setTimeStart(startTime);


            List<LocalTime> tt = new ArrayList<>();
            tt.add(startTime);
            itinerary.setArrivals(tt);
            itinerary.setDistance(0);
            itinerary.setTime(0);

            double curTime=0;

            int wpCount = 0;
            while (!wayPoints.isEmpty()&&((wpCount++)<=capacity))

            {
                MatrixElement me = Matrix.NearestMap(curWP, matrix, wayPoints);
                WayPoint tryWP = me.getWayPoint();
                curTime += me.getTime();

                // visit this WP
                itinerary.getWayPointList().add(tryWP);
                itinerary.setDistance(itinerary.getDistance() + me.getDistance());
                itinerary.setTime(itinerary.getTime() + me.getTime());
                itinerary.getArrivals().add(startTime.plus((long) curTime, ChronoUnit.MILLIS));

                curWP = tryWP;
                wayPoints.remove(tryWP);
            }

            // return to base
            itinerary.getWayPointList().add(start);
            itinerary.setDistance(itinerary.getDistance() + Matrix.DistanceBetweenMap(curWP,start,matrix));
            itinerary.setTime(itinerary.getTime() + Matrix.TimeBetweenMap(curWP,start,matrix));

            curTime+=Matrix.TimeBetweenMap(curWP,start,matrix);
            itinerary.getArrivals().add(startTime.plus((long) curTime, ChronoUnit.MILLIS));

            // now complete itinerary
            itinerary.setTimeEnd(startTime.plus((long) curTime, ChronoUnit.MILLIS));
            result.getItineraries().add(itinerary);
            result.setDistanceTotal(result.getDistanceTotal() + itinerary.getDistance());
            result.setTimeTotal(result.getTimeTotal() + itinerary.getTime());

            //System.out.println("Iteration " + itCount + " completed in "+ (System.currentTimeMillis()-startTime));
        }
        // now complete result
        result.setItineraryQty(itCount);

        long elapsedTime = System.currentTimeMillis() - elTime;

        System.out.println("\n\nTotal time: " + round(result.getTimeTotal()) / 60000 + " min");
        System.out.println("Total distance: " + round(result.getDistanceTotal()) / 1000 + " km");
        System.out.println("Cars assigned: " + result.getItineraryQty());
        System.out.println("Calculated in: " + elapsedTime + " ms\n");

        return result;
    }
}
