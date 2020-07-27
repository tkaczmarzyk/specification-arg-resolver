/**
 * Copyright 2014-2020 the original author or authors.
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
package net.kaczmarzyk.utils.interceptor;

import org.hibernate.EmptyInterceptor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HibernateStatementInterceptor extends EmptyInterceptor {

	private static List<String> statements = new CopyOnWriteArrayList<>();

	@Override
	public String onPrepareStatement(String sql) {
		statements.add(sql);
		return super.onPrepareStatement(sql);
	}

	public static List<String> getInterceptedStatements() {
		return statements;
	}

	public static void clearInterceptedStatements() {
		statements.clear();
	}
}
