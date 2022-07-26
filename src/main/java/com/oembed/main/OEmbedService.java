package com.oembed.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class OEmbedService {
	private final String[] schemes = {"http", "https"};
	private final UrlValidator urlValidator = new UrlValidator(schemes);
	private final ObjectMapper mapper = new ObjectMapper();
	public static final Logger logger = LoggerFactory.getLogger(OEmbedService.class.getPackage().getName());
	private JsonNode providers = null;
	
	public OEmbedService() {
		try {
			providers = mapper.readTree(new URL("https://oembed.com/providers.json"));
		} catch (IOException e) {
			System.out.println("Can't load providers from 'https://oembed.com/providers.json', trying to load local providers instead.");
		}
		
		ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("providers.json");
        
        try {
			providers = mapper.readTree(resource);
		} catch (IOException e) {
			logger.debug("[Fatal] Can't load providers.json in local. This application cannot service.");
		}
	}
	
	public Boolean validateURL(String url) {
		return urlValidator.isValid(url);
	}
	
	public String getProvider(String raw) throws URISyntaxException, RuntimeException, Exception { //search from providers.json
		if (providers == null) throw new RuntimeException("Fatal error occurred on server, contact to developer. 서버에 심각한 오류가 발생하였습니다. 관리자에게 문의하세요.");
		
		URL url = null;
		try {
			URI uri = new URI(raw);
			url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), "/");
		} catch (URISyntaxException | MalformedURLException e) {
			throw new URISyntaxException(null, "Malformed URL. 형식에 맞지 않는 URL입력입니다.");
		}

		String result = null;
		Iterator<JsonNode> iter = providers.iterator();
		while (iter.hasNext()) {
			JsonNode node = iter.next();
			String provider_url = node.get("provider_url").asText();
			if (!provider_url.equals(url.toString()) && !provider_url.equals(url.toString().substring(0, url.toString().length() - 1))) continue;
			
			result = provider_url;
		}
		
		System.out.println(result);
		return result;
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
			throw new ClientProtocolException("Error recieving response data. 응답 데이터 수신중에 오류가 발생했습니다.");
		}
        if (entity == null) throw new ClientProtocolException("응답 데이터가 없습니다.");
		
        Map<String, String> data = null;
    	try {
			data = mapper.readValue(EntityUtils.toString(entity), Map.class);
    	} catch (JsonParseException e){ //결과값이 Map이 아닐경우 (e.g, "Bad Request", "Not Found") readValue에 실패하면서 발생 
    		throw new ParseException("No content from given url. 해당 url에서 컨텐츠를 가져올 수 없습니다.");
		} catch (IOException e) {
			throw new IOException("Error occurred processing reponse data. 응답 데이터를 처리하는중에 오류가 발생했습니다.");
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