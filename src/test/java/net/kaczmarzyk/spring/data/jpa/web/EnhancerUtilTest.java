/**
 * Copyright 2014-2022 the original author or authors.
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
package net.kaczmarzyk.spring.data.jpa.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.kaczmarzyk.spring.data.jpa.domain.Like;


public class EnhancerUtilTest {

	public static interface TestIface {
	}
	
	@Test
	public void createsObjectWithWorkingToString() {
		
		Like likeSpec = new Like(null, "firstName", "Homer");
		
		TestIface wrappedSpec = EnhancerUtil.<TestIface>wrapWithIfaceImplementation(TestIface.class, likeSpec);
		
		assertThat(wrappedSpec.toString()).isEqualTo("TestIface[Like [pattern=%Homer%]]");
	}
}
