package com.oembed.main;

import java.io.IOException;
import java.net.MalformedURLException;
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
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OEmbedService {
	private final String[] schemes = {"http", "https"};
	private final UrlValidator urlValidator = new UrlValidator(schemes);
	private final ObjectMapper mapper = new ObjectMapper();
	private JSONObject providers = null;
	
	public OEmbedService() {
		providers = loadProviders("https://oembed.com/providers.json");
	}
	
	public JSONObject loadProviders(String url) {
		
		return providers;
	}
	
	public Boolean validateURL(String url) {
		return urlValidator.isValid(url);
	}
	
	public String getProvider(String url) {
		String host = null;
		
		try {
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
			throw new RuntimeException("부정한 URL");
		}
		
		return host;
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