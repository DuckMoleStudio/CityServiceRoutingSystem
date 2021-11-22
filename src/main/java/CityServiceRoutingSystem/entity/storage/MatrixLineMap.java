package CityServiceRoutingSystem.entity.storage;

import CityServiceRoutingSystem.entity.WayPoint;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

// main matrix-forming class, contains all other WPs with time/distance from given WP.
// Matrix is a map of these maps mapped by WP itself
public class MatrixLineMap {
    private Map<WayPoint, TimeDistancePair> distances;
}