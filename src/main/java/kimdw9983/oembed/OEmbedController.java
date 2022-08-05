package kimdw9983.oembed;

import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class OEmbedController {
    private final OEmbedService OEmbedService;
    public static final Logger logger = LoggerFactory.getLogger(OEmbedController.class.getPackage().getName());
    
    public OEmbedController(OEmbedService OEmbedService) {
        this.OEmbedService = new OEmbedService();
    }

    private ResponseEntity<OEmbedMessage> respond(Object data, HttpStatus status) {
        OEmbedMessage message = new OEmbedMessage();
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        message.setStatus(status);
        message.setData(data);

        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }
    
    @RequestMapping("/")
    public ResponseEntity<OEmbedMessage> index() {
        String data = "Enter url. url을 입력해주세요. e.g){domain}/oembed/https://www.youtube.com/watch?v=FtutLA63Cp8";

        return respond(data, HttpStatus.OK);
    }
    
    @RequestMapping("/oembed")
    public ResponseEntity<OEmbedMessage> help() {
        String data = "Enter url. url을 입력해주세요. e.g){domain}/oembed/https://www.youtube.com/watch?v=FtutLA63Cp8";
        return respond(data, HttpStatus.OK);
    }

    @GetMapping("/oembed/**")
    public ResponseEntity<OEmbedMessage> oembed(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String url = requestURL.split("/oembed/")[1];
        String query = request.getQueryString();

        if (url.contains(":/") && !url.contains("://")) url = url.replace(":/", "://");
        if (query != null) url = url + "?" + query;

        try {
            OEmbedService.validateURL(url);
        } catch (ResponseStatusException e) {
            return respond(e.getReason(), e.getStatus());
        };
        
        String provider;
        try {
            provider = OEmbedService.getProvider(url);
        } catch (ResponseStatusException e) {
            return respond(e.getReason(), e.getStatus());
        }
        
        JsonNode data = null;
        try {
            data = OEmbedService.getOembedData(provider, url);
        } catch (ResponseStatusException e) {
            return respond(e.getReason(), e.getStatus());
        } 
        
        return respond(data, HttpStatus.OK);
    }
}