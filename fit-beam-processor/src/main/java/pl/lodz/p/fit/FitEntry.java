package pl.lodz.p.fit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class FitEntry implements Serializable {

    private static final long serialVersionUID = 42L;

    private final String city;
    private final long time;
    private final int distance;

    public FitEntry() {
        this.city = "";
        this.time = 0;
        this.distance = 0;
    }

    @JsonCreator
    public FitEntry(@JsonProperty("city") String city,
                    @JsonProperty("time") long time,
                    @JsonProperty("distance") int distance) {

        this.city = city;
        this.time = time;
        this.distance = distance;
    }

    public String getCity() {
        return city;
    }

    public long getTime() {
        return time;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FitEntry fitEntry = (FitEntry) o;
        return time == fitEntry.time &&
                distance == fitEntry.distance &&
                Objects.equals(city, fitEntry.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, time, distance);
    }

    @Override
    public String toString() {
        return "FitEntry{" +
                "city='" + city + '\'' +
                ", time=" + time +
                ", distance=" + distance +
                '}';
    }
}
