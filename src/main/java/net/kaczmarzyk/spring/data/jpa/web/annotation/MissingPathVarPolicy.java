/**
 * Copyright 2014-2025 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web.annotation;

/**
 * MissingPathVarPolicy is used to specify behaviour when there is at least one missing pathVar.
 * In most cases it means that the mapping is wrong, so the exception should be thrown.
 * It is also possible, that user has multiple paths with different pathVars, and then IGNORE is the solution.
 *
 * @author Tomasz Kaczmarzyk
 * @author Jakub Radlica
 * @author Konrad Hajduga (Tratif sp. z o.o.)
 */
public enum MissingPathVarPolicy {

	IGNORE, EXCEPTION

}
