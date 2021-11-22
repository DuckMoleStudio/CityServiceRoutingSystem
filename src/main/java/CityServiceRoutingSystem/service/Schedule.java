package CityServiceRoutingSystem.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class Schedule {
    public static boolean isActive(String schedule, LocalDate date)
    {
        switch (schedule)
        {
            case "all": case "еж.": return true;
            case "ex Sundays":
                return !date.getDayOfWeek().equals(DayOfWeek.SUNDAY);
            case "пн.вт.чт.пт.сб.вс.":
                return !date.getDayOfWeek().equals(DayOfWeek.WEDNESDAY);
            case "сб.":
                return date.getDayOfWeek().equals(DayOfWeek.SATURDAY);
            case "пт.":
                return date.getDayOfWeek().equals(DayOfWeek.FRIDAY);
            case "ср.":
                return date.getDayOfWeek().equals(DayOfWeek.WEDNESDAY);
            case "вт.пт.":
                return date.getDayOfWeek().equals(DayOfWeek.TUESDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.FRIDAY);
            case "ср.сб.":
                return date.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.SATURDAY);
            case "вт.чт.сб.":
                return date.getDayOfWeek().equals(DayOfWeek.TUESDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.THURSDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.SATURDAY);
            case "пн.ср.пт.":
                return date.getDayOfWeek().equals(DayOfWeek.MONDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.WEDNESDAY)
                        || date.getDayOfWeek().equals(DayOfWeek.FRIDAY);
            case "10,31":
                return date.getDayOfMonth() == 10 || date.getDayOfMonth() == 31;
            case "пт. (1,3,5)":
                return date.getDayOfWeek().equals(DayOfWeek.FRIDAY)
                        && ((date.getDayOfMonth() < 8)
                        || (date.getDayOfMonth() > 14 && date.getDayOfMonth() < 22)
                        || (date.getDayOfMonth() > 28));
        }
        return true;
    }
}
