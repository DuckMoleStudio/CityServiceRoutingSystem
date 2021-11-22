package CityServiceRoutingSystem.entity.storage;
import CityServiceRoutingSystem.entity.WayPoint;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode

// internal
public class Hop {
    WayPoint from,to;
}