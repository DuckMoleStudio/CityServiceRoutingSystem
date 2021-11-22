package CityServiceRoutingSystem.service.greedyAlgos;

import CityServiceRoutingSystem.entity.Car;
import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.entity.result.Itinerary;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixElement;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.service.Matrix;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class TimeslotVRP {
    public static Result Calculate(
            List<WayPoint> wayPoints,
            Map<WayPoint, MatrixLineMap> matrix,
            int carCapacity,
            LocalTime startTime){

        long elTime = System.currentTimeMillis();

        Result result = new Result();
        result.setMethodUsed("Simple Greedy Algorithm for collection with timeslots V.01");
        List<Itinerary> ii = new ArrayList<>();
        result.setItineraries(ii);

        WayPoint start = new WayPoint();
        WayPoint dump = new WayPoint();


        // get start & dump
        for(WayPoint ww: wayPoints)
        {
            switch (ww.getType())
            {
                case Garbage_Dump:
                    dump = ww;
                    break;

                case Base:
                    start = ww;
                    break;

                default:
                    break;
            }
        }
        wayPoints.remove(start);
        wayPoints.remove(dump);

        int itCount = 0;

        while (!wayPoints.isEmpty())
        {
            long itTime = System.currentTimeMillis();

            Itinerary itinerary = new Itinerary(); // new car & new itinerary start here
            itinerary.setCar(new Car("Kamaz Othodov # "+(++itCount), WayPointType.Garbage_Site, carCapacity));

            List<WayPoint> ll = new ArrayList<>();
            ll.add(start);
            itinerary.setWayPointList(ll);


            itinerary.setTimeStart(startTime);
            int curLoad = 0;

            List<WayPoint> subWP = new ArrayList<>(wayPoints); // suitable for current itinerary
            WayPoint curWP = start;
            LocalTime curTime = startTime;

            List<LocalTime> tt = new ArrayList<>();
            tt.add(curTime);
            itinerary.setArrivals(tt);

            long minWait=-1; // reset
            long curWait;
            List<WayPoint> tmpWP = new ArrayList<>(); // for temporary non-available

            while (!subWP.isEmpty())
            {
                // get nearest operational
                // 1. get nearest

                MatrixElement me = Matrix.NearestMap(curWP, matrix, subWP);
                WayPoint tryWP = me.getWayPoint();
                double curDistance = me.getDistance();
                long tryTime = (long) me.getTime();


                // 2. check temporal availability
                if(Duration.between(
                        curTime.plus(
                                Duration.ofMillis(tryTime)),
                        tryWP.getTimeClose()).getSeconds()>0)
                {
                    curWait = Duration.between
                            (curTime.plus(Duration.ofMillis(tryTime)),tryWP.getTimeOpen()).getSeconds();

                    if(curWait <= 0)
                    {
                        // timeslot ok, visit this WP, if capacity permits, else visit dump
                        if(curLoad+tryWP.getCapacity()<=itinerary.getCar().getCapacity())
                        {
                            curLoad+=tryWP.getCapacity();
                            itinerary.getWayPointList().add(tryWP);
                            itinerary.setDistance(itinerary.getDistance() + curDistance);
                            curTime = curTime.plus(Duration.ofMillis(tryTime)); // enroute time
                            itinerary.getArrivals().add(curTime);
                            curTime = curTime.plus(tryWP.getDuration()); // loading time
                            curWP = tryWP;
                            wayPoints.remove(tryWP);
                            subWP.remove(tryWP);
                            subWP.addAll(tmpWP); // ones we were early for, try on next pass
                            tmpWP.clear();
                            minWait = -1;
                        }
                        else
                        {
                            // visit dump site
                            itinerary.getWayPointList().add(dump);
                            curDistance = Matrix.DistanceBetweenMap(curWP,dump,matrix);
                            tryTime = (long) Matrix.TimeBetweenMap(curWP,dump,matrix);
                            itinerary.setDistance(itinerary.getDistance()+curDistance);
                            curTime = curTime.plus(Duration.ofMillis(tryTime)); // enroute time
                            itinerary.getArrivals().add(curTime);
                            curTime = curTime.plus(Duration.ofMillis(dump.getDuration().toMillis()*curLoad)); // unloading time
                            curWP = dump;
                            subWP.addAll(tmpWP); // ones we were early for, try on next pass
                            tmpWP.clear();
                            minWait = -1;
                            curLoad = 0;
                        }
                    }
                    else // we are early, remove for this pass but restore later
                    {
                        if((curWait < minWait) || (minWait < 0)) {minWait = curWait;}
                        tmpWP.add(tryWP);
                        subWP.remove(tryWP);
                        if(subWP.isEmpty()) // we are early everywhere!
                        {
                            curTime = curTime.plus(Duration.ofSeconds(minWait)); // wait for 1st to open
                            minWait = -1;
                            subWP.addAll(tmpWP); // ones we were early for, try on next pass
                            tmpWP.clear();
                        }
                    }
                }
                else{subWP.remove(tryWP);} // we are late for this WP, discard totally
            }
            // now complete itinerary (but first dump & rtb)

            // visit dump site (if not already there!)
            if(!curWP.equals(dump))
            {
                itinerary.getWayPointList().add(dump);
                double curDistance = Matrix.DistanceBetweenMap(curWP, dump, matrix);
                long tryTime = (long) Matrix.TimeBetweenMap(curWP, dump, matrix);
                itinerary.setDistance(itinerary.getDistance() + curDistance);
                curTime = curTime.plus(Duration.ofMillis(tryTime)); // enroute time
                itinerary.getArrivals().add(curTime);
                curTime = curTime.plus(Duration.ofMillis(dump.getDuration().toMillis() * curLoad)); // unloading time
            }
            // rtb
            itinerary.getWayPointList().add(start);
            double curDistance = Matrix.DistanceBetweenMap(dump,start,matrix);
            long tryTime = (long) Matrix.TimeBetweenMap(dump,start,matrix);
            itinerary.setDistance(itinerary.getDistance()+curDistance);
            curTime = curTime.plus(Duration.ofMillis(tryTime)); // enroute time
            itinerary.getArrivals().add(curTime);

            itinerary.setTimeEnd(curTime);
            itinerary.setTime(Duration.between(startTime,curTime).toMillis());
            result.getItineraries().add(itinerary);
            result.setDistanceTotal(result.getDistanceTotal()+itinerary.getDistance());
            result.setTimeTotal(result.getTimeTotal()+itinerary.getTime());

            System.out.println("Iteration " + itCount + " completed in "+ (System.currentTimeMillis()-itTime));
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
