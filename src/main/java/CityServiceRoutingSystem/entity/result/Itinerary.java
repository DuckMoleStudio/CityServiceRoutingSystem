package CityServiceRoutingSystem.entity.result;

import CityServiceRoutingSystem.entity.Car;
import CityServiceRoutingSystem.entity.WayPoint;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode

// Single route for a single car
public class Itinerary {

    private Car car;
    private List<WayPoint> wayPointList;
    private List<LocalTime> arrivals;
    private LocalTime timeStart, timeEnd;
    private double distance;
    private double time;
}