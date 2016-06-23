package net.kaczmarzyk.spring.data.jpa.domain;

import net.kaczmarzyk.spring.data.jpa.domain.PathSpecification;
import net.kaczmarzyk.spring.data.jpa.utils.Converter;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * <p>Filters with geo-coordinates. It determines whether a point is in the square with center (latitude, longitude) and side = 2*distance.</p>
 * 
 * <p>Supports field types: BigDecimal.</p>
 * 
 * Created by d.romantsov on 23.06.2016.
 */
public class GeoPosition<T> extends PathSpecification<T> {

    private Converter converter;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal distance;

    public GeoPosition(String path, String... args) {
        super(path);
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException("Expected exactly three argument (the fragment to match against), but got: " + Arrays.toString(args));
        } else {
            latitude = new BigDecimal(args[0]);
            longitude = new BigDecimal(args[1]);
            distance = new BigDecimal(args[2]);
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String[] paths = path.split(";");
        if (paths.length != 2) {
            throw new IllegalArgumentException("Expected exactly two paths united with \";\", but got: " + path);
        }
        Expression latitudeEx = root.get(paths[0]);
        Expression longitudeEx = root.get(paths[1]);

        return builder.and(
                builder.greaterThan(latitudeEx, latitude.subtract(distance)),
                builder.lessThan(latitudeEx, latitude.add(distance)),
                builder.greaterThan(longitudeEx, longitude.subtract(distance)),
                builder.lessThan(longitudeEx, longitude.add(distance)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPosition)) return false;
        if (!super.equals(o)) return false;

        GeoPosition<?> that = (GeoPosition<?>) o;

        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        return distance != null ? distance.equals(that.distance) : that.distance == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (distance != null ? distance.hashCode() : 0);
        return result;
    }
}