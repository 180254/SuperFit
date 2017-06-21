package pl.lodz.p.fit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class Key implements Serializable {

    private static final long serialVersionUID = 42L;

    private final String city;

    public Key() {
        this.city = "";
    }

    @JsonCreator
    public Key(@JsonProperty("city") String city) {
        this.city = city;
    }


    public Key(FitEntry fitEntry) {
        this.city = fitEntry.getCity();
    }

    public String getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return Objects.equals(city, key.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city);
    }

    @Override
    public String toString() {
        return "Key{" +
                "city='" + city + '\'' +
                '}';
    }
}
