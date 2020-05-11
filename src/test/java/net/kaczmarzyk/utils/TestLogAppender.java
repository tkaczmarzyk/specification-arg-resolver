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
package net.kaczmarzyk.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

/**
 * A dummy log appender to make assertions on logs...
 * Which is not a good way to assert things, but at the time of implementation,
 * I didn't have any better idea to validate what happens under the hood of Hibernate
 * 
 * @author Tomasz Kaczmarzyk
 */
public class TestLogAppender<E> implements Appender<E> {

	private static final List<String> LOGS = new ArrayList<>();
	
	public static List<String> getInterceptedLogs() {
		return new ArrayList<>(LOGS);
	}
	
	public static void clearInterceptedLogs() {
		LOGS.clear();
	}
	
	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isStarted() {
		return true;
	}

	@Override
	public void setContext(Context context) {
	}

	@Override
	public Context getContext() {
		return null;
	}

	@Override
	public void addStatus(Status status) {
	}

	@Override
	public void addInfo(String msg) {
	}

	@Override
	public void addInfo(String msg, Throwable ex) {
	}

	@Override
	public void addWarn(String msg) {
	}

	@Override
	public void addWarn(String msg, Throwable ex) {
	}

	@Override
	public void addError(String msg) {
	}

	@Override
	public void addError(String msg, Throwable ex) {
	}

	@Override
	public void addFilter(Filter<E> newFilter) {
	}

	@Override
	public void clearAllFilters() {
	}

	@Override
	public List<Filter<E>> getCopyOfAttachedFiltersList() {
		return Collections.emptyList();
	}

	@Override
	public FilterReply getFilterChainDecision(E event) {
		return FilterReply.NEUTRAL;
	}

	@Override
	public String getName() {
		return TestLogAppender.class.getName();
	}

	@Override
	public void doAppend(E event) throws LogbackException {
		TestLogAppender.LOGS.add(event.toString());
	}

	@Override
	public void setName(String name) {
	}

}
