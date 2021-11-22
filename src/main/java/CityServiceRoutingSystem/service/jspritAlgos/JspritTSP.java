package CityServiceRoutingSystem.service.jspritAlgos;

import CityServiceRoutingSystem.entity.Car;
import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.WayPointType;
import CityServiceRoutingSystem.entity.result.Itinerary;
import CityServiceRoutingSystem.entity.result.Result;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import CityServiceRoutingSystem.service.Matrix;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.lang.Math.round;

public class JspritTSP {
    public static Result Calculate(
            List<WayPoint> wayPointList,
            Map<WayPoint, MatrixLineMap> matrix,
            int capacity,
            int iterations,
            LocalTime workStart)
    {
        long elTime = System.currentTimeMillis();

        // ---- prepare services (points) and get base for car start ----
        List<Service> services = new ArrayList<>();
        WayPoint startWP = new WayPoint();
        for(WayPoint c_wp: wayPointList)
        {
            if (c_wp.getType() == WayPointType.Base) {
                startWP = c_wp;
            }
            else {
                services.add(Service.Builder
                        .newInstance(String.valueOf(wayPointList.indexOf(c_wp)))
                        .addSizeDimension(0, 1)
                        .setLocation(Location.newInstance(String.valueOf(wayPointList.indexOf(c_wp))))
                        .build());
            }
        }

        String start = String.valueOf(wayPointList.indexOf(startWP));


        /*
         * get a vehicle type-builder and build a type
         */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleTypeRita")
                .addCapacityDimension(0, capacity);
        VehicleType vehicleTypeRita = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle
         */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicleRita");
        vehicleBuilder.setStartLocation(Location.newInstance(start));
        vehicleBuilder.setType(vehicleTypeRita);
        VehicleImpl vehicleRita = vehicleBuilder.build();


        //define a matrix-builder building a NON-symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix
                .Builder.newInstance(false);

        // ---- transfer our matrix to jsprit matrix ----
        for(int jj=0;jj<wayPointList.size();jj++)
            for(int kk=0;kk<wayPointList.size();kk++)
                if(jj!=kk)
                {
                    costMatrixBuilder.addTransportDistance(
                            String.valueOf(jj),
                            String.valueOf(kk),
                            Matrix.DistanceBetweenMap(wayPointList.get(jj),wayPointList.get(kk),matrix));
                    costMatrixBuilder.addTransportTime(
                            String.valueOf(jj),
                            String.valueOf(kk),
                            Matrix.TimeBetweenMap(wayPointList.get(jj),wayPointList.get(kk),matrix));
                }
                else
                {
                    costMatrixBuilder.addTransportDistance(
                            String.valueOf(jj),
                            String.valueOf(kk),
                            Double.POSITIVE_INFINITY);
                    costMatrixBuilder.addTransportTime(
                            String.valueOf(jj),
                            String.valueOf(kk),
                            Double.POSITIVE_INFINITY);
                }
        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();


        // --- SET UP THE ROUTING PROBLEM ----
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE)
                .setRoutingCost(costMatrix);

        // ----- add cars and services (points) to the problem
        vrpBuilder.addVehicle(vehicleRita);
        for(Service ss: services)
        {
            vrpBuilder.addJob(ss);
        }

        VehicleRoutingProblem problem = vrpBuilder.build();


        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem)
                .setProperty(Jsprit.Parameter.THREADS, "4")
                .buildAlgorithm();
        algorithm.setMaxIterations(iterations);

        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        // --- PARSE SOLUTION TO OUR RESULT ----
        Result result = new Result();
        result.setMethodUsed("Jsprit Algorithm, simple TSP");
        List<Itinerary> ii = new ArrayList<>();
        result.setItineraries(ii);

        List<VehicleRoute> list = new ArrayList<VehicleRoute>(bestSolution.getRoutes());
        Collections.sort(list , new com.graphhopper.jsprit.core.util.VehicleIndexComparator());

        int routeNu = 1;
        for (VehicleRoute route : list)
        {
            Itinerary curItinerary = new Itinerary();
            curItinerary.setCar(new Car(route.getVehicle().getId() + (routeNu),
                    WayPointType.Paid_Parking, 0));

            WayPoint curWP = startWP;
            List<WayPoint> ll = new ArrayList<>();
            ll.add(curWP);
            curItinerary.setWayPointList(ll);
            double costs = 0;
            ArrayList<LocalTime> arrivals = new ArrayList<LocalTime>();
            arrivals.add(workStart);
            curItinerary.setArrivals(arrivals);

            TourActivity prevAct = route.getStart();
            for (TourActivity act : route.getActivities())
            {
                String jobId;
                if (act instanceof TourActivity.JobActivity)
                {
                    jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    curItinerary.getWayPointList().add(wayPointList.get(Integer.parseInt(jobId)));
                    curItinerary.getArrivals().
                            add(workStart.plus(Math.round(act.getArrTime()), ChronoUnit.MILLIS));

                }

                double c = problem.getTransportCosts().getTransportCost(
                        prevAct.getLocation(),
                        act.getLocation(),
                        prevAct.getEndTime(),
                        route.getDriver(),
                        route.getVehicle());
                c += problem.getActivityCosts().getActivityCost(
                        act,
                        act.getArrTime(),
                        route.getDriver(),
                        route.getVehicle());
                costs += c;

                prevAct = act;
            }



            double c = problem.getTransportCosts().getTransportCost(
                    prevAct.getLocation(),
                    route.getEnd().getLocation(),
                    prevAct.getEndTime(),
                    route.getDriver(),
                    route.getVehicle());
            c += problem.getActivityCosts().getActivityCost(
                    route.getEnd(),
                    route.getEnd().getArrTime(),
                    route.getDriver(),
                    route.getVehicle());
            costs += c;


            routeNu++;

            // ---- complete itinerary ----
            curItinerary.getWayPointList().add(startWP);
            curItinerary.getArrivals().
                    add(workStart.plus(Math.round(route.getEnd().getArrTime()), ChronoUnit.MILLIS));
            curItinerary.setDistance(costs);
            curItinerary.setTime(route.getEnd().getArrTime());
            result.getItineraries().add(curItinerary);
            result.setDistanceTotal(result.getDistanceTotal() + curItinerary.getDistance());
            result.setTimeTotal(result.getTimeTotal() + curItinerary.getTime());

        }
        // ---- complete result -----
        result.setItineraryQty(routeNu-1);

        long elapsedTime = System.currentTimeMillis() - elTime;
        System.out.println("\n\nTotal time: " + round(result.getTimeTotal()) / 60000 + " min");
        System.out.println("Total distance: " + round(result.getDistanceTotal()) / 1000 + " km");
        System.out.println("Cars assigned: " + result.getItineraryQty());
        System.out.println("Calculated in: " + elapsedTime + " ms\n");

        return result;
    }
}
