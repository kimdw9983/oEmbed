package kimdw9983.oembed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
      importProviders();
    } catch (IOException e) {
      logger.error("[Fatal] Can't load providers.json in local. This application cannot service!!!");
      logger.error("[Fatal] Can't load providers.json in local. This application cannot service!!!");
      logger.error("[Fatal] Can't load providers.json in local. This application cannot service!!!");
    }
  }

  public void importProviders() throws IOException {
    try {
      providers = mapper.readTree(new URL("https://oembed.com/providers.json"));
    } catch (IOException e) {
      logger.warn("Can't load providers from 'https://oembed.com/providers.json', trying to load local providers instead.");
    }
    
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("providers.json");

    providers = mapper.readTree(resource);
  }
  
  public void validateURL(String url) throws ResponseStatusException {
    if (!urlValidator.isValid(url)) {
      logger.info("Malformed URL\t" + url);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed url. 잘못된 url형식 입니다.\n Url must contain protocol(https://) field.");
    }
  }
  
  public String getProvider(String raw) throws ResponseStatusException { //search from providers.json, is this autodiscovery btw?
    if (providers == null) {
      logger.error("[Fatal] No providers.json found.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Fatal error occurred on server, please contact to developer. 서버에 심각한 오류가 발생하였습니다. 관리자에게 문의해주세요.");
    }
    
    URL url;
    URI uri;
    logger.info("getProvider() raw \t" + raw);
    try {
      uri = new URI(raw);
      url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), "/");
      logger.info("getProvider() constructed url\t" + url);
    } catch (URISyntaxException | MalformedURLException e) {
      logger.info("Malformed URL \t" + raw);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed URL. 잘못된 url형식 입니다.");
    }

    String result = null;
    try {
      for(Iterator<JsonNode> iter = providers.iterator(); iter.hasNext();) {
        JsonNode provider_node = iter.next();
        JsonNode endpoints_node = (JsonNode) provider_node.get("endpoints");
        String uriString = uri.toString();
        for(Iterator<JsonNode> iiter = ((ArrayNode) endpoints_node).elements(); iiter.hasNext();) {
          JsonNode endpoint = iiter.next();
          ArrayNode schemes = (ArrayNode) endpoint.get("schemes");
          if (schemes != null) {
            for(Iterator<JsonNode> iiiter = schemes.elements(); iiiter.hasNext();) {
              String scheme = iiiter.next().asText();
              String re = scheme.replace(".{format}", ".json").replace("/", "\\/").replace("*", ".*");
              if (!uriString.matches(re)) continue;
              
              logger.info("getProvider() provider found\t" + endpoint.get("url"));
              result = endpoint.get("url").asText();
              return result;
            }
          }  else { //case when there's no scheme definition on endpoint like beautiful.ai, boxofficebuz.com
            String re = provider_node.get("provider_url").asText();
            if (!uriString.matches(re)) continue;
            
            logger.info("getProvider() provider without scheme, found\t" + endpoint.get("url"));
            result = endpoint.get("url").asText();
            return result;
          }
        }
      }
    } catch(Exception e) {
      logger.error("Provider fetch failed\n{}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred during fetching providers. 제공자 정보를 불러오는중에 오류가 발생했습니다.");
    }

    logger.info("No provider found on URL\t" + raw);
    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No provider found. 해당 url의 제공자가 없습니다.");
  }

  public JsonNode getOembedData(String provider, String url) throws ResponseStatusException {
    String request_url;
    if (provider.contains("*")) {
      request_url = provider.replace("*", url);
    } else {
      provider = provider.replace("{format}", "json");
      request_url = provider + (provider.endsWith("/") ? url : "?url=" + url);
    }
    logger.info("request url\t" + request_url);

    HttpEntity entity = null;
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet httpget = new HttpGet(request_url);
      HttpResponse response = client.execute(httpget);
      entity = response.getEntity();
    } catch (IOException e) {
      logger.error("Error on recieving response data, provider " + provider + " url " + url);
      logger.error("{}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error recieving response data. 응답 데이터 수신중에 오류가 발생했습니다.");
    }
    if (entity == null) throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "응답 데이터가 없습니다.");

    JsonNode data = null;
    try {
      data = mapper.readTree(EntityUtils.toString(entity));
    } catch (IOException e) {
      logger.info("No content from given url\t" + url);
      throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No content from given url. 해당 url에서 컨텐츠를 가져올 수 없습니다.");
    }

    return data;
  }
}