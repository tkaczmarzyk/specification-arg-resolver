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
import java.util.Locale;
import java.util.Map;

import org.springframework.web.context.request.NativeWebRequest;

public class FakeWebRequest implements NativeWebRequest {

	private Map<String, Object> attrs = new HashMap<>();
	private Map<String, String[]> params = new HashMap<>();
	
	@Override
	public String getHeader(String headerName) {
		return null;
	}

	@Override
	public String[] getHeaderValues(String headerName) {
		return null;
	}

	@Override
	public Iterator<String> getHeaderNames() {
		return null;
	}

	@Override
	public String getParameter(String paramName) {
		return null;
	}

	@Override
	public String[] getParameterValues(String paramName) {
		return params .get(paramName);
	}

	@Override
	public Iterator<String> getParameterNames() {
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return null;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public boolean checkNotModified(long lastModifiedTimestamp) {
		return false;
	}

	@Override
	public boolean checkNotModified(String etag) {
		return false;
	}

	@Override
	public boolean checkNotModified(String etag, long lastModifiedTimestamp) {
		return false;
	}

	@Override
	public String getDescription(boolean includeClientInfo) {
		return null;
	}

	@Override
	public Object getAttribute(String name, int scope) {
		return attrs.get(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		attrs.put(name, value);
	}

	@Override
	public void removeAttribute(String name, int scope) {
	}

	@Override
	public String[] getAttributeNames(int scope) {
		return null;
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback, int scope) {
	}

	@Override
	public Object resolveReference(String key) {
		return null;
	}

	@Override
	public String getSessionId() {
		return null;
	}

	@Override
	public Object getSessionMutex() {
		return null;
	}

	@Override
	public Object getNativeRequest() {
		return null;
	}

	@Override
	public Object getNativeResponse() {
		return null;
	}

	@Override
	public <T> T getNativeRequest(Class<T> requiredType) {
		return null;
	}

	@Override
	public <T> T getNativeResponse(Class<T> requiredType) {
		return null;
	}

	public void setParameterValues(String paramName, String... values) {
		params.put(paramName, values);
	}

}
