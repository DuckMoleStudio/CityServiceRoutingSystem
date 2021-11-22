package CityServiceRoutingSystem.inputData;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class geoData {
    String type;
    double[] coordinates;
}

