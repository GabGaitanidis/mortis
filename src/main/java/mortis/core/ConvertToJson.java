package mortis.core;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
public class ConvertToJson {
    public static JsonNode convertToJson(String s) throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode  = mapper.readTree(s);
        return rootNode;
        
    }
}
