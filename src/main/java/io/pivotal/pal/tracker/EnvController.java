package io.pivotal.pal.tracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EnvController {
    private final Map<String, String> variables;

    public EnvController(
            @Value("${port:NOT SET}") String port,
            @Value("${memory.limit:NOT SET}") String memoryLimit,
            @Value("${cf.instance.index:NOT SET}") String cfInstanceIndex,
            @Value("${cf.instance.addr:NOT SET}") String cfInstanceAddress
    ){

        variables = new HashMap<>();
        variables.put("PORT", port);
        variables.put("MEMORY_LIMIT", memoryLimit);
        variables.put("CF_INSTANCE_INDEX", cfInstanceIndex);
        variables.put("CF_INSTANCE_ADDR", cfInstanceAddress);

    }

    @GetMapping("/env")
    public Map<String, String> getEnv() {
        return variables;
    }
}
