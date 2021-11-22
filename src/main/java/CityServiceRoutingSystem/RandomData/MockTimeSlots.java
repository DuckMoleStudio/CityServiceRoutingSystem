package CityServiceRoutingSystem.RandomData;

import CityServiceRoutingSystem.entity.WayPoint;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

public class MockTimeSlots {
    public static void fill(List<WayPoint> wayPoints,
                            int timeStartMin,
                            int timeStartMax,
                            DistType timeDist,
                            int intervalMin,
                            int intervalMax,
                            DistType intervalDist,
                            int maxCapacity)
    {
        Random random = new Random();
        for(WayPoint wp: wayPoints)
        {
            LocalTime startTime = LocalTime.parse("00:00");
            switch (timeDist)
            {
                case Equal:
                    startTime = LocalTime.of
                            (random.nextInt(timeStartMax-timeStartMin)+timeStartMin,0);
                    break;

                case Gauss:
                    startTime = LocalTime.of
                            ((int)(RandomMethods.truncGauss()*(timeStartMax-timeStartMin))+timeStartMin,0);
                    break;

                case Ascend:
                    startTime = LocalTime.of
                            ((int)(RandomMethods.expoAsc()*(timeStartMax-timeStartMin))+timeStartMin,0);
                    break;

                case Descend:
                    startTime = LocalTime.of
                            ((int)(RandomMethods.expoDesc()*(timeStartMax-timeStartMin))+timeStartMin,0);
                    break;

                default:
                    break;
            }

            LocalTime endTime = LocalTime.parse("00:00");
            switch (intervalDist)
            {
                case Equal:
                    endTime = startTime.plus(Duration.ofHours
                            (random.nextInt(intervalMax-intervalMin)+intervalMin));
                    break;

                case Gauss:
                    endTime = startTime.plus(Duration.ofHours
                            ((int)(RandomMethods.truncGauss()*(intervalMax-intervalMin)+intervalMin)));
                    break;

                case Ascend:
                    endTime = startTime.plus(Duration.ofHours
                            ((int)(RandomMethods.expoAsc()*(intervalMax-intervalMin)+intervalMin)));
                    break;

                case Descend:
                    endTime = startTime.plus(Duration.ofHours
                            ((int)(RandomMethods.expoDesc()*(intervalMax-intervalMin)+intervalMin)));
                    break;

                default:
                    break;
            }
            wp.setTimeOpen(startTime);
            wp.setTimeClose(endTime);
            wp.setCapacity(random.nextInt(maxCapacity)+1);
            String[] schedules = {
                    "пн.вт.чт.пт.сб.вс.", "сб.", "вт.пт.", "еж.",
                    "пт.", "ср.", "ср.сб.", "вт.чт.сб.", "10,31",
                    "пт. (1,3,5)", "пн.ср.пт.",
                    "еж.","еж.","еж.","еж.","еж.","еж.","еж.","еж.","еж.",}; // 55
            wp.setSchedule(schedules[random.nextInt(schedules.length)]);
        }
    }
}
