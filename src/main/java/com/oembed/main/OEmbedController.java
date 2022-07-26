package com.oembed.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OEmbedController {
	private final OEmbedService OEmbedService;
	
	public OEmbedController(OEmbedService OEmbedService) {
		this.OEmbedService = new OEmbedService();
	}
	
	@RequestMapping("/")
    public ResponseEntity<OEmbedMessage> index() {
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        
        message.setStatus(HttpStatus.OK);
        message.setData("Enter url. url을 입력해주세요. e.g){domain}/oembed/https://www.youtube.com/watch?v=FtutLA63Cp8");
        
        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
	
	@RequestMapping("/oembed")
    public ResponseEntity<OEmbedMessage> help() {
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        
        message.setStatus(HttpStatus.OK);
        message.setData("Enter url. url을 입력해주세요. e.g){domain}/oembed/https://www.youtube.com/watch?v=FtutLA63Cp8");

        return new ResponseEntity<>(message, headers, HttpStatus.OK);
	}

	@RequestMapping("/oembed/**")
    public ResponseEntity<OEmbedMessage> oembed(HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();
		String url = requestURL.split("/oembed/")[1];
		String query = request.getQueryString();
		if (query != null) url = url + "?" + query;
		
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        
        try {
			OEmbedService.validateURL(url);
		} catch (MalformedURLException e) {
			message.setStatus(HttpStatus.BAD_REQUEST);
			message.setData(e.getMessage());
			
			return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
		};
        
        String provider;
        try { //url에서 oembed요청을 보낼 provider 탐색
        	provider = OEmbedService.getProvider(url);
        } catch (RuntimeException e) {
        	message.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.SERVICE_UNAVAILABLE);
        } catch (URISyntaxException e) {
        	message.setStatus(HttpStatus.BAD_REQUEST);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            message.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
        	message.setStatus(HttpStatus.NOT_IMPLEMENTED);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.NOT_IMPLEMENTED);
        }
        
    	Map<String, String> data = null;
		try {
			data = OEmbedService.getOembedData(provider, url);
		} catch (ParseException e) {
			message.setStatus(HttpStatus.NOT_IMPLEMENTED);
	        message.setData(e.getMessage());
	        
	        return new ResponseEntity<>(message, headers, HttpStatus.NOT_IMPLEMENTED);
		} catch (ClientProtocolException e) {
			message.setStatus(HttpStatus.NOT_IMPLEMENTED);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.NOT_IMPLEMENTED);
		}  catch (IOException e) {
			message.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		} 
		
        message.setStatus(HttpStatus.OK);
		message.setData(data);
		
        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
	
	@GetMapping(value = "/test")
    public ResponseEntity<OEmbedMessage> test() {
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
    }
}