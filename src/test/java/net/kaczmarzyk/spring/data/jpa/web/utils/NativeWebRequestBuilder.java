package net.kaczmarzyk.spring.data.jpa.web.utils;

import org.springframework.web.context.request.NativeWebRequest;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NativeWebRequestBuilder {

	private NativeWebRequest nativeWebRequest = mock(NativeWebRequest.class);

	private NativeWebRequestBuilder() {}

	public static NativeWebRequestBuilder nativeWebRequest() {
		return new NativeWebRequestBuilder();
	}

	public NativeWebRequestBuilder withParameterValues(String parameter, String... values) {
		when(nativeWebRequest.getParameterValues(parameter))
				.thenReturn(values);
		return this;
	}

	public NativeWebRequest build() {
		return nativeWebRequest;
	}
}
