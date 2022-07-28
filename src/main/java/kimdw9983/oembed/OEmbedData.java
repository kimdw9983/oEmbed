package kimdw9983.oembed;

import lombok.Data;

@Data
public class OEmbedData { 
	//Serialized Response Entity를 POJO로 다루게 될 경우 사용할 DTO.
	//지금은 Jackson-databind의 ObjectMapper를 이용해 바로 Map으로 변환 후 출력
    private String author_name;
    private String author_url;
    private String title;
    private String type;
    
    private String thumbnail_url;
    private String thumbnail_width;
    private String thumbnail_height;
    
    private String html;
    private String height;
    private String width;
    
    private String provider_name;
    private String provider_url;
    private String version; 
}