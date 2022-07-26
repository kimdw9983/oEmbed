package com.oembed.main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OEmbedService {
	private final String[] schemes = {"http", "https"};
	private final UrlValidator urlValidator = new UrlValidator(schemes);
	private final ObjectMapper mapper = new ObjectMapper();
	private JsonNode providers = null;
	
	public OEmbedService() {
		try {
			providers = mapper.readTree(new URL("https://oembed.com/providers.json"));
		} catch (IOException e) {
			System.out.println("https://oembed.com/providers.json에서 providers를 로드하지 못했습니다. 로컬 파일로 대체합니다.");
		}
		
		ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("providers.json");
        
        try {
			providers = mapper.readTree(resource);
		} catch (IOException e) {
			System.out.println("[심각] providers.json 파일을 로드할 수 없습니다. 어플리케이션은 url에서 oembed provider를 구할 수 없습니다.");
		}
	}
	
	public Boolean validateURL(String url) {
		return urlValidator.isValid(url);
	}
	
	public String getProvider(String url) throws URISyntaxException, RuntimeException {
		String provider = null;
		if (providers == null) throw new RuntimeException("서버에 심각한 오류가 발생하였습니다.");
		
		try {
			String host = new URI(url).getHost();
			/*
			 * java.net.URL has a bunch of problems -- its equals method does a DNS lookup.
			 * http://foo.example.com => 245.10.10.1
			 * http://example.com => 245.10.10.1
			 * IP를 위와같이 바인딩했다고 가정하자. 이경우
			 * 
			 * URL("foo.example.com").equals(URL("example.com")) => true 
			 * 두개의 url은 같은 것으로 취급되므로, 중간자 공격에 취약해진다. 따라서 URI를 사용한다.
			 */
			provider = host.startsWith("www.") ? host.substring(4) : host;
		} catch (URISyntaxException e) {
			throw new URISyntaxException(null, "형식에 맞지 않는 URL입력입니다.");
		}
		
		
		
		return provider;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getOembedData(String provider, String url) throws ClientProtocolException, IOException, ParseException {
		provider = "https://www.youtube.com/oembed?url="; //TEST

		HttpEntity entity = null;
        try {
        	HttpClient client = HttpClientBuilder.create().build();
        	HttpGet httpget = new HttpGet(provider + url);
			HttpResponse response = client.execute(httpget);
			entity = response.getEntity();
		} catch (IOException e) {
			throw new ClientProtocolException("응답 데이터 수신중에 오류가 발생했습니다.");
		}
        if (entity == null) throw new ClientProtocolException("응답 데이터가 없습니다.");
		
        Map<String, String> data = null;
    	try {
			data = mapper.readValue(EntityUtils.toString(entity), Map.class);
    	} catch (JsonParseException e){ //결과값이 Map이 아닐경우 (e.g, "Bad Request", "Not Found") readValue에 실패하면서 발생 
    		throw new ParseException("해당 url에서 컨텐츠를 가져올 수 없습니다.");
		} catch (IOException e) {
			throw new IOException("응답 데이터를 처리하는중에 오류가 발생했습니다.");
		}
    	
		/*
		 * OEmbedData data = null; //POJO방식의 데이터로 바인딩 할 경우
		 * try { 
		 * data = new OEmbedData();
		 * ObjectMapper mapper = new ObjectMapper(); 
		 * data = mapper.readValue(EntityUtils.toString(entity), OEmbedData.class);
		 * 
		 * } catch (JSONException | ParseException | IOException e) { throw new
		 * ParseException("응답 데이터를 처리하는중에 오류가 발생했습니다."); }
		 */

		return data;
	}
}