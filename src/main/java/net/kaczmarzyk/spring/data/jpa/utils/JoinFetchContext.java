package net.kaczmarzyk.spring.data.jpa.utils;

import org.springframework.web.context.request.NativeWebRequest;

import javax.persistence.criteria.Fetch;
import java.util.HashMap;
import java.util.List;

public class JoinFetchContext {

	private static final String NOT_EVALUATED_JOIN_FETCH = JoinFetchContext.class.getName() + ".ATTRIBUTE_KEY";
	private static final String EVALUATED_JOIN_FETCH = JoinFetchContext.class.getName() + ".ATTRIBUTE_KEY_2";

	private HashMap<String, List<JoinFetchDefinition>> notEvaluatedJoinFetch;
	private HashMap<String, Fetch<?, ?>> evaluatedJoinFetch;

	public JoinFetchContext(NativeWebRequest request) {
		this.evaluatedJoinFetch = (HashMap<String, Fetch<?, ?>>) request.getAttribute(EVALUATED_JOIN_FETCH, NativeWebRequest.SCOPE_REQUEST);

		if (this.evaluatedJoinFetch == null) {
			this.evaluatedJoinFetch = new HashMap<>();
			request.setAttribute(EVALUATED_JOIN_FETCH, evaluatedJoinFetch, NativeWebRequest.SCOPE_REQUEST);
		}
	}

	public void putEvaluatedFetch(String alias, Fetch<?, ?> fetch) {
		this.evaluatedJoinFetch.put(alias, fetch);
	}

	public Fetch<?, ?> getEvaluated(String alias) {
		return this.evaluatedJoinFetch.get(alias);
	}
}
