package pl.lodz.p.fit;

import java.io.Serializable;
import java.util.Objects;

public class FitKey implements Serializable {

    private static final long serialVersionUID = 42L;

    private final String city;
    private final long time;

    public FitKey(FitEntry fitEntry) {
        this.city = fitEntry.getCity();
        this.time = fitEntry.getTime();
    }

    public FitKey(String city, long time) {
        this.city = city;
        this.time = time;
    }

    public String getCity() {
        return city;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FitKey fitKey = (FitKey) o;
        return time == fitKey.time &&
                Objects.equals(city, fitKey.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, time);
    }

    @Override
    public String toString() {
        return "FitKey{" +
                "city='" + city + '\'' +
                ", time=" + time +
                '}';
    }
}
