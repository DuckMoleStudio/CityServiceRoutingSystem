package CityServiceRoutingSystem.entity.storage;

import CityServiceRoutingSystem.entity.WayPoint;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

// used for json serialisation & de-serialisation, file storage. We need a list to have strict sorting.
public class MatrixStorageLine {
    private WayPoint wayPoint;
    private List<TimeDistancePair> distances;
}