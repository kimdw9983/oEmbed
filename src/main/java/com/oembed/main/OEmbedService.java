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
import com.fasterxml.jackson.databind.node.ArrayNode;

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
			logger.warn("Can't load providers from 'https://oembed.com/providers.json', trying to load local providers instead.");
		}
		
		ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("providers.json");
        
        try {
			providers = mapper.readTree(resource);
		} catch (IOException e) {
			logger.error("[Fatal] Can't load providers.json in local. This application cannot service.");
		}
	}
	
	public void validateURL(String url) throws MalformedURLException {
		if (!urlValidator.isValid(url)) {
			logger.debug("Malformed URL\t" + url);
			throw new MalformedURLException("Malformed url. 잘못된 url형식 입니다.");
		}
	}
	
	public String getProvider(String raw) throws URISyntaxException, RuntimeException, Exception, IOException { //search from providers.json, is this autodiscovery btw?
		if (providers == null) {
			logger.error("[Fatal] No providers.json found.");
			throw new RuntimeException("Fatal error occurred on server, please contact to developer. 서버에 심각한 오류가 발생하였습니다. 관리자에게 문의해주세요.");
		}
		
		URL url;
		URI uri;
		logger.debug("getProvider() raw \t" + raw);
		try {
			uri = new URI(raw);
			url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), "/");
			logger.debug("getProvider() constructed url\t" + url);
		} catch (URISyntaxException | MalformedURLException e) {
			logger.debug("Malformed URL \t" + raw);
			throw new URISyntaxException(null, "Malformed URL. 형식에 맞지 않는 URL입력입니다.");
		}

		String result = null;
		try {
			for(Iterator<JsonNode> iter = providers.iterator(); iter.hasNext();) {
				JsonNode provider_node = iter.next();
				logger.debug(provider_node.toString());

				JsonNode endpoints_node = (JsonNode) provider_node.get("endpoints");
				JsonNode endpoints = ((ArrayNode) endpoints_node).elements().next();
				ArrayNode schemes = (ArrayNode) endpoints.get("schemes");
				
				String uriString = uri.toString();
				//logger.debug("getProvider() uriString\t" + uriString);
				if (schemes != null) {
					for(Iterator<JsonNode> iiter = schemes.elements(); iiter.hasNext();) {
						String scheme = iiter.next().asText();
						String re = scheme.replace("/", "\\/").replace("*", ".*");
						if (!uriString.matches(re)) continue;
						
						logger.debug("getProvider() provider found\t" + endpoints.get("url"));
						result = endpoints.get("url").asText();	
						return result;
					}
				} else { //providers like Beautiful.AI has no schemes
					String re = provider_node.get("provider_url").asText();
					if (!uriString.matches(re)) continue;

					logger.debug("getProvider() provider without scheme, found\t" + endpoints.get("url"));
					result = endpoints.get("url").asText();
					return result;
				}
				
	
				// String provider_url = endpoints_node.get("provider_url").asText();
				// result = provider_url;
				// return result;
				//if (!provider_url.equals(url.toString()) && !provider_url.equals(url.toString().substring(0, url.toString().length() - 1))) continue;
			}
		} catch(NullPointerException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		logger.debug("No provider found on URL\t" + raw);
		throw new Exception("No provider found. 해당 url의 제공자가 없습니다.");

		// 	//String result = null;
		// 	try {
		// 		JsonNode node = (JsonNode) provider.get("endpoints");
		// 		JsonNode endpoints = ((ArrayNode) node).elements().next();
		// 		ArrayNode schemes = (ArrayNode) endpoints.get("schemes");
				
		// 		String uriString = uri.toString();
		// 		logger.debug("getProvider() uriString\t" + uriString);
		// 		for(Iterator<JsonNode> iter = schemes.elements(); iter.hasNext();) {
		// 			String scheme = iter.next().asText();
		// 			String re = scheme.replace("/", "\\/").replace("*", ".*");
		// 			if (!uriString.matches(re)) continue;

		// 			result = endpoints.get("url").asText();	
		// 			logger.debug("getProvider() provider found\t" + result);
		// 			break;
		// 		}
		// 	} catch(Exception e) {
		// 		logger.error("Url provider found but error occured during regex. \t");
		// 		e.printStackTrace();
		// 		throw new IOException("Error occured during scanning provider. 제공자를 찾는 도중 오류가 발생했습니다.");
		// 	}
		// 	if (result == null) {
		// 		logger.debug("Url provider found but no matching regex\t" + raw);
		// 		throw new Exception("No provider found. 해당 url의 제공자가 없습니다.");
		// 	}
			
		// 	return result;
		// }
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
			//logger.debug("getOembedData()\tentity\t" + entity.toString());
		} catch (IOException e) {
			logger.warn("Error recieving response data, provider " + provider + " url " + url);
			throw new IOException("Error recieving response data. 응답 데이터 수신중에 오류가 발생했습니다.");
		}
        if (entity == null) throw new ClientProtocolException("응답 데이터가 없습니다.");
		
        Map<String, String> data = null;
    	try {
			data = mapper.readValue(EntityUtils.toString(entity), Map.class);
    	} catch (JsonParseException e){ //결과값이 Map이 아닐경우 (e.g, "Bad Request", "Not Found") readValue에 실패하면서 발생 
    		logger.debug("No content from given url\t" + url);
    		throw new ParseException("No content from given url. 해당 url에서 컨텐츠를 가져올 수 없습니다.");
		} catch (IOException e) {
			logger.warn("Error occurred processing reponse data\t" + url);
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