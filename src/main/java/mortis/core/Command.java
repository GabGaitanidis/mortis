package mortis.core;

import java.util.Map;

public class Command {

    private String module;
    private String action;
    private Map<String, Object> params;
    
    public Command(String module, String action, Map<String, Object> params) {
        this.module = module;
        this.action = action;
        this.params = params;
    }

   
    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public Object get(String key) {
        return params.get(key);
    }
}