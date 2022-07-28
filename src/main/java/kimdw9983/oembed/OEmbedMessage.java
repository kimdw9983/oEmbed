package kimdw9983.oembed;
import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class OEmbedMessage {
    private HttpStatus status;
    private Object data;

    public OEmbedMessage() {
    	this.status = HttpStatus.NOT_FOUND; 
        this.data = null;
    }
}