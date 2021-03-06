package CityServiceRoutingSystem.service;

import CityServiceRoutingSystem.entity.*;

import CityServiceRoutingSystem.entity.storage.DoubleMatrix;
import CityServiceRoutingSystem.entity.storage.MatrixElement;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.entity.storage.TimeDistancePair;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;

import com.graphhopper.util.Parameters;
import com.graphhopper.util.StopWatch;

import java.util.*;

public class Matrix {

    public static Map<WayPoint, MatrixLineMap> FillGHMulti4Map(
            List<WayPoint> wayPoints, String osmFile, String dir, boolean turns, boolean curbs)
    // using Graph Hopper on real map, with 4 threads, store in HashMap
    {
        StopWatch sw = new StopWatch().start();
        Map<WayPoint, MatrixLineMap> matrix = new HashMap<>();

        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation(dir);

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(
                new Profile("car1").setVehicle("car").setWeighting("shortest").setTurnCosts(false),
                new Profile("car2").setVehicle("car").setWeighting("fastest").setTurnCosts(true).putHint("u_turn_costs", 60)
        );

        // this enables speed mode for the profile we call "car" here
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car1"),new CHProfile("car2"));

        hopper.importOrLoad();

        String profile = "car1";
        if(turns) profile = "car2";

        int section = (int)Math.ceil(wayPoints.size() / 4);

        String finalProfile = profile;
        class sub {
            void fill (int from, int to){
                for(int i=from; i < to; i++)
                {
                    MatrixLineMap line = new MatrixLineMap();
                    line.setDistances(new HashMap<>());

                    for(int j=0; j < wayPoints.size(); j++)
                    {
                        if(i==j)
                        {
                            line.getDistances().put(wayPoints.get(j),
                                    new TimeDistancePair(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY));
                        }
                        else
                        {
                            GHRequest req = new GHRequest(
                                    wayPoints.get(i).getLat()
                                    , wayPoints.get(i).getLon()
                                    , wayPoints.get(j).getLat()
                                    , wayPoints.get(j).getLon())
                                    .setProfile(finalProfile)
                                    .setAlgorithm(Parameters.Algorithms.ASTAR_BI);

                            if(curbs)
                            {
                                req.setCurbsides(Arrays.asList("right", "right"));
                                //req.putHint("u_turn_costs", 6000);
                            }

                            req.putHint("instructions", false);
                            req.putHint("calc_points", false);
                            req.putHint(Parameters.Routing.FORCE_CURBSIDE, false);

                            GHResponse res = hopper.route(req);

                            double distance = Math.round(res.getBest().getDistance()); // to fit in storage line
                            double time = res.getBest().getTime();

                            if (res.hasErrors()) {
                                throw new RuntimeException(res.getErrors().toString());
                            }

                            line.getDistances().put(wayPoints.get(j),new TimeDistancePair(time,distance));
                        }
                    }

                    int div = 50;
                    if(wayPoints.size() <= 50) div = wayPoints.size()-1;
                    if(i%(wayPoints.size()/div) == 0)
                        System.out.print("."); // progress indicator, optimized for 50 dots or fewer

                    synchronized (matrix) {matrix.put(wayPoints.get(i),line);}
                }
            }
        }

        Thread one = new Thread(new Runnable() {
            @Override
            public void run() {
                sub sub1 = new sub();
                sub1.fill(0,section);
            }
        });

        Thread two = new Thread(new Runnable() {
            @Override
            public void run() {
                sub sub2 = new sub();
                sub2.fill(section,section*2);
            }
        });

        Thread three = new Thread(new Runnable() {
            @Override
            public void run() {
                sub sub3 = new sub();
                sub3.fill(section*2,section*3);
            }
        });

        Thread four = new Thread(new Runnable() {
            @Override
            public void run() {
                sub sub4 = new sub();
                sub4.fill(section*3,wayPoints.size());
            }
        });

// start threads
        one.start();
        two.start();
        three.start();
        four.start();

// Wait for threads above to finish
        try{
            one.join();
            two.join();
            three.join();
            four.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupt Occurred");
            e.printStackTrace();
        }
        System.out.println("\nMatrix calculated in: " + sw.stop().getSeconds() + " s\n");
        return matrix;
    }

    public static DoubleMatrix FillGHDouble(
            List<WayPoint> wayPoints, String osmFile, String dir, WayPoint base)
    // using Graph Hopper on real map, store in two (maybe) HashMaps, time-based
    {
        StopWatch sw = new StopWatch().start();

        Map<WayPoint, MatrixLineMap> matrixGood = new HashMap<>();
        Map<WayPoint, MatrixLineMap> matrixBad = new HashMap<>();
        List<WayPoint> wayPointsGood = new ArrayList<>();
        List<WayPoint> wayPointsBad = new ArrayList<>();

        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmFile);
        hopper.setGraphHopperLocation(dir);

        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(
                new Profile("car1").setVehicle("car").setWeighting("shortest").setTurnCosts(false),
                new Profile("car2").setVehicle("car").setWeighting("fastest").setTurnCosts(true).putHint("u_turn_costs", 60)
        );

        // this enables speed mode for the profile we call "car" here
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("car1"),new CHProfile("car2"));

        hopper.importOrLoad();

        for(WayPoint wp : wayPoints)
        {
            GHRequest req = new GHRequest(
                    base.getLat()
                    , base.getLon()
                    , wp.getLat()
                    , wp.getLon())
                    .setProfile("car2")
                    .setCurbsides(Arrays.asList("any", "right"))
                    .setAlgorithm(Parameters.Algorithms.ASTAR_BI);

            req.putHint("instructions", false);
            req.putHint("calc_points", false);
            //req.putHint(Parameters.Routing.FORCE_CURBSIDE, false);

            GHResponse res = hopper.route(req);

            if (res.hasErrors())  //throw new RuntimeException(res.getErrors().toString());
            {wayPointsBad.add(wp);}
            else wayPointsGood.add(wp);

        }
        if(wayPointsGood.size()>0)
        {
            wayPointsGood.add(base);
            System.out.println("\n\nCalculating matrix for " + wayPointsGood.size() + " relevant points\n");
            matrixGood = Matrix.FillGHMulti4Map(wayPointsGood,osmFile,dir,true,true);
        }

        if(wayPointsBad.size()>0)
        {
            wayPointsBad.add(base);
            System.out.println("\n\nCalculating matrix for " + wayPointsBad.size() + " irrelevant points\n");
            matrixBad = Matrix.FillGHMulti4Map(wayPointsBad,osmFile,dir,false,false);
        }

        System.out.println("\nMatrix calculated in: " + sw.stop().getSeconds() + " s\n");
        return new DoubleMatrix(matrixGood,matrixBad,wayPointsGood,wayPointsBad);
    }


    public static double DistanceBetweenMap(WayPoint start, WayPoint end, Map<WayPoint,MatrixLineMap> matrix)
    {
        return matrix.get(start).getDistances().get(end).getDistance();
    }

    public static double TimeBetweenMap(WayPoint start, WayPoint end, Map<WayPoint,MatrixLineMap> matrix)
    {
        return matrix.get(start).getDistances().get(end).getTime();
    }

    public static MatrixElement NearestMap(
            WayPoint start,
            Map<WayPoint,MatrixLineMap> matrix,
            List<WayPoint> existing)
    {
        MatrixLineMap ml = matrix.get(start);
        Set<Map.Entry<WayPoint,TimeDistancePair>> distances = ml.getDistances().entrySet();
        MatrixElement result = new MatrixElement();
        double minTime = Double.POSITIVE_INFINITY;

        for (Map.Entry<WayPoint,TimeDistancePair> me: distances)
        {
            if(me.getValue().getTime() < minTime && existing.contains(me.getKey()))
            {
                minTime = me.getValue().getTime();
                result.setDistance(me.getValue().getDistance());
                result.setTime(me.getValue().getTime());
                result.setWayPoint(me.getKey());
            }
        }


        return result;
    }



}

