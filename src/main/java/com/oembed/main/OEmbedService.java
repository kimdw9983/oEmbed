package com.oembed.main;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

@Service
public class OEmbedService {
	String[] schemes = {"http", "https"};
	private final UrlValidator urlValidator = new UrlValidator(schemes);
	
	public Boolean validateURL(String url) {
		return urlValidator.isValid(url);
	}
	
	public String getProvider(String url) {
		String host;
		
		try {
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
			throw new RuntimeException("부정한 URL");
		}
		
		if (host == null) throw new RuntimeException("host가 없음");
		
		return host;
	}
}