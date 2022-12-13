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
package net.kaczmarzyk.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
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
public class TestLogAppender extends AppenderBase<ILoggingEvent> {

	private static final List<String> LOGS = new ArrayList<>();

	public static List<String> getInterceptedLogs() {
		return new ArrayList<>(LOGS);
	}
	
	public static void clearInterceptedLogs() {
		LOGS.clear();
	}

	@Override
	protected void append(ILoggingEvent iLoggingEvent) {
		LOGS.add(iLoggingEvent.getMessage());
	}
}
