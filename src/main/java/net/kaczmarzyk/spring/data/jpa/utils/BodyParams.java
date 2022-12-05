package net.kaczmarzyk.spring.data.jpa.utils;

import java.util.Collection;

public interface BodyParams {

	Collection<String> getParamValues(String paramKey);
}
