package com.oembed.main;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

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
        
        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
	
	@RequestMapping("/oembed")
    public ResponseEntity<OEmbedMessage> help() {
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        
        message.setStatus(HttpStatus.OK);
        message.setData("url을 입력해주세요");

        return new ResponseEntity<>(message, headers, HttpStatus.OK);
	}

	@RequestMapping("/oembed/**")
    public ResponseEntity<OEmbedMessage> oembed(HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();
		String url = requestURL.split("/oembed/")[1];
		String queryString = request.getQueryString();
		
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        
        if(!OEmbedService.validateURL(url)) {
            message.setStatus(HttpStatus.FORBIDDEN);
            message.setData("유효하지 않은 url입니다.");
            
            return new ResponseEntity<>(message, headers, HttpStatus.FORBIDDEN);
        };
        
        String provider;
        try {
        	provider = OEmbedService.getProvider(url);
        } catch (RuntimeException e) {
        	message.setStatus(HttpStatus.FORBIDDEN);
            message.setData(e.getMessage());
            
            return new ResponseEntity<>(message, headers, HttpStatus.FORBIDDEN);
        }
        
        try {
        	provider = "https://www.youtube.com/oembed?url=";
        	System.out.println(url);
        	System.out.println(queryString);
        	System.out.println(provider + url + "?" +  queryString);        	
			HttpResponse response = HttpClientBuilder.create().build().execute(new HttpGet("https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=dBD54EZIrZo"));
			 
			String json_string = EntityUtils.toString(response.getEntity(), "UTF-8");
			JSONObject temp1 = new JSONObject(json_string);
			message.setData(json_string);
			//message.setStatus(HttpStatus.OK);
            //message.setData(oembedInfo);
            
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
	
	@GetMapping(value = "/test")
    public ResponseEntity<OEmbedMessage> test() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); //타임아웃 설정 5초
        factory.setReadTimeout(5000);//타임아웃 설정 5초
        RestTemplate restTemplate = new RestTemplate(factory);
		
		OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
        HttpEntity<?> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<OEmbedMessage> result = restTemplate.exchange("http://127.0.0.1:8080/oembed/test", HttpMethod.GET, entity, OEmbedMessage.class);
			message.setStatus(HttpStatus.FORBIDDEN);
	        message.setData(result.getStatusCodeValue());
	        
		} catch (HttpClientErrorException e) {
			message.setStatus(HttpStatus.FORBIDDEN);
	        message.setData("잘못된 url");
	        
			return new ResponseEntity<>(message, headers, HttpStatus.FORBIDDEN);
		}
        
        return new ResponseEntity<>(message, headers, HttpStatus.FORBIDDEN);
    }
	
	@RequestMapping("/errorPage")
    public ModelAndView errorPage(String reason) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("errorPage");
        //mv.addObject("random_string", "asdjkfjasdkf");

        return mv;
    }
}