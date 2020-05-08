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
package net.kaczmarzyk.spring.data.jpa.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * @author Tomasz Kaczmarzyk
 */
public class MockWebRequest implements NativeWebRequest {

	private Map<String, List<String>> parameters = new HashMap<>();
	private Map<String, Object> attributes = new HashMap<>();
	private String pathInfo;
	

	public MockWebRequest(String pathInfo) {
		this.pathInfo = pathInfo;
	}
	
	@Override
	public String[] getParameterValues(String paramName) {
		List<String> values = parameters.get(paramName);
		return values != null ? values.toArray(new String[0]) : null;
	}
	
	@Override
	public Object getNativeRequest() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setPathInfo(pathInfo);
		return req;
	}
	
	@Override
	public Object getAttribute(String name, int scope) {
		return attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		attributes.put(name, value);
	}
	
	@Override
	public String getHeader(String headerName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getHeaderValues(String headerName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> getHeaderNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getParameter(String paramName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> getParameterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContextPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteUser() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String role) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkNotModified(long lastModifiedTimestamp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkNotModified(String etag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkNotModified(String etag, long lastModifiedTimestamp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDescription(boolean includeClientInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String name, int scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getAttributeNames(int scope) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void registerDestructionCallback(String name, Runnable callback, int scope) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object resolveReference(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSessionId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getSessionMutex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getNativeResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getNativeRequest(Class<T> requiredType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getNativeResponse(Class<T> requiredType) {
		throw new UnsupportedOperationException();
	}

}
