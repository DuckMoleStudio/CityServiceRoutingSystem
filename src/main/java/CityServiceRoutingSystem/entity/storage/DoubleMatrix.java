package CityServiceRoutingSystem.entity.storage;
import CityServiceRoutingSystem.entity.WayPoint;
import CityServiceRoutingSystem.entity.storage.MatrixLineMap;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode

// two matrices & WPlists, for "good" & "bad" waypoints (i.e. reachable & unreachable on the right side)
public class DoubleMatrix {
    Map<WayPoint, MatrixLineMap> mapGood;
    Map<WayPoint, MatrixLineMap> mapBad;
    List<WayPoint> wayPointsGood;
    List<WayPoint> wayPointsBad;
}