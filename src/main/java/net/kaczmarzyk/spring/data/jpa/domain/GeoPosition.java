/**
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk.spring.data.jpa.domain;

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
    private DistanceHelper distanceHelper = new DistanceHelper();

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal distance;
    private boolean allowNull = false;
    private DistanceUnit distanceUnit = DistanceUnit.METER;

    public GeoPosition(String path[], String[] args) {
        this(path, args, null);
    }
    
    public GeoPosition(String[] path, String[] args, String[] config) {
        super(path);
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException("Expected exactly three argument (the fragment to match against), but got: " + Arrays.toString(args));
        } else {
            latitude = new BigDecimal(args[0]);
            longitude = new BigDecimal(args[1]);
            distance = new BigDecimal(args[2]);
            allowNull = Boolean.parseBoolean(config[0]);
            if (config.length > 1) {
                distanceUnit = DistanceUnit.valueOf(config[1].toUpperCase());
            }
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (path.length != 2) {
            throw new IllegalArgumentException("Expected exactly two paths united with \";\", but got: " + path);
        }

        Expression latitudeEx = root.get(path[0]);
        Expression longitudeEx = root.get(path[1]);

        BigDecimal latitudeDistance;
        BigDecimal longitudeDistance;
        switch (distanceUnit) {
            case DEGREE:
                latitudeDistance=distance;
                longitudeDistance=distance;
                break;
            case METER:
                latitudeDistance=distanceHelper.transformLatitude(distance, latitude);
                longitudeDistance=distanceHelper.transformLongitude(distance);
                break;
            default:
                throw new IllegalStateException("Unknown Distant unit. Unit: "+distanceUnit.name());
        }

        Predicate latitudeGt = builder.greaterThan(latitudeEx, latitude.subtract(latitudeDistance));
        Predicate latitudeLs = builder.lessThan(latitudeEx, latitude.add(latitudeDistance));
        Predicate longitudeGt = builder.greaterThan(longitudeEx, longitude.subtract(longitudeDistance));
        Predicate longitudeLs = builder.lessThan(longitudeEx, longitude.add(longitudeDistance));

        return allowNull ?
                builder.and(
                        builder.or(
                                builder.isNull(latitudeEx),
                                builder.and(latitudeGt, latitudeLs)),
                        builder.or(
                                builder.isNull(latitudeEx),
                                builder.and(longitudeGt, longitudeLs))) :
                builder.and(latitudeGt, latitudeLs, longitudeGt, longitudeLs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoPosition)) return false;
        if (!super.equals(o)) return false;

        GeoPosition<?> that = (GeoPosition<?>) o;

        if (allowNull != that.allowNull) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;
        if (!distance.equals(that.distance)) return false;
        return distanceUnit == that.distanceUnit;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + distance.hashCode();
        result = 31 * result + (allowNull ? 1 : 0);
        result = 31 * result + distanceUnit.hashCode();
        return result;
    }

    public class DistanceHelper {
        /**
         * In meters
         */
        public final BigDecimal PARALLELS_DEGREE_LENGTH_ON_EQUATOR = new BigDecimal(111321.377778);
        /**
         * In meters
         */
        public final BigDecimal MERIDIANS_DEGREE_LENGTH = new BigDecimal(111134.86111);

        private int scale = 6;
        /**
         *
         * @param meters
         * @return degree
         */
        public BigDecimal transformLatitude(BigDecimal meters, BigDecimal latitude) {
            return meters.divide(PARALLELS_DEGREE_LENGTH_ON_EQUATOR
                    .multiply(new BigDecimal(Math.cos(latitude.doubleValue()))), scale, BigDecimal.ROUND_HALF_DOWN);
        }

        /**
         *
         * @param meters
         * @return degree
         */
        public BigDecimal transformLongitude(BigDecimal meters) {
            return meters.divide(MERIDIANS_DEGREE_LENGTH, scale, BigDecimal.ROUND_HALF_DOWN);
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }
    }

    public enum DistanceUnit {
        DEGREE, METER
    }
}