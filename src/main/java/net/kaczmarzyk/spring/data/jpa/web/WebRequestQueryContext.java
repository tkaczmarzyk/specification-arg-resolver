package net.kaczmarzyk.spring.data.jpa.web;

import java.util.HashMap;

import org.springframework.web.context.request.NativeWebRequest;

import net.kaczmarzyk.spring.data.jpa.utils.QueryContext;

/**
 * @author Tomasz Kaczmarzyk
 */
public class WebRequestQueryContext implements QueryContext {

	private static final String ATTRIBUTE_KEY = WebRequestQueryContext.class.getName() + ".ATTRIBUTE_KEY";
	
	private HashMap<String, Object> contextMap;

	public WebRequestQueryContext(NativeWebRequest request) {
		this.contextMap = (HashMap<String, Object>) request.getAttribute(ATTRIBUTE_KEY, NativeWebRequest.SCOPE_REQUEST);
		if (this.contextMap == null) {
			this.contextMap = new HashMap<>();
			request.setAttribute(ATTRIBUTE_KEY, contextMap, NativeWebRequest.SCOPE_REQUEST);
		}
	}
	
	@Override
	public Object get(String key) {
		return contextMap.get(key);
	}

	@Override
	public void put(String key, Object value) {
		contextMap.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextMap == null) ? 0 : contextMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WebRequestQueryContext other = (WebRequestQueryContext) obj;
		if (contextMap == null) {
			if (other.contextMap != null)
				return false;
		} else if (!contextMap.equals(other.contextMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WebRequestQueryContext [contextMap=" + contextMap + "]";
	}
}
