/**
 * Copyright 2014-2023 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.utils;

import javax.persistence.criteria.Path;
import java.lang.reflect.ParameterizedType;

/**
 * <p>Util class used to obtain information about collection element class type from specifications (e.g. used for `isMember`, `isNotMember`).</p>
 * @author Hubert Gotfryd (Tratif sp. z o.o.)
 */
public abstract class ClassTypeReflectionUtils {

    public static Class<?> getElementTypeFromPath(String path, Path<?> rootPath) {
        try {
            String typeOnPathClassName = ((ParameterizedType) rootPath.getParentPath().getJavaType().getDeclaredField(path).getGenericType()).getActualTypeArguments()[0].getTypeName();
            return Class.forName(typeOnPathClassName);
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
