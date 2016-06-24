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

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal distance;
    private boolean allowNull = false;

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
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (path.length != 2) {
            throw new IllegalArgumentException("Expected exactly two paths united with \";\", but got: " + path);
        }

        Expression latitudeEx = root.get(path[0]);
        Expression longitudeEx = root.get(path[1]);

        Predicate latitudeGt = builder.greaterThan(latitudeEx, latitude.subtract(distance));
        Predicate latitudeLs = builder.lessThan(latitudeEx, latitude.add(distance));
        Predicate longitudeGt = builder.greaterThan(longitudeEx, longitude.subtract(distance));
        Predicate longitudeLs = builder.lessThan(longitudeEx, longitude.add(distance));

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