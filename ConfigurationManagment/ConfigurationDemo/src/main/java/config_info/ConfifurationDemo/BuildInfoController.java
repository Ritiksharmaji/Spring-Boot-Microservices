package config_info.ConfifurationDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class BuildInfoController {

    // These values will come from Config Server if available, otherwise default values will be used
    @Value("${build.id:1001}")
    private String buildId;

    @Value("${build.version:1.0.0}")
    private String buildVersion;

    @Value("${build.name:Default-Build}")
    private String buildName;

    @GetMapping("/build-info")
    public String getBuildInfo() {
        return "BuildId: " + buildId + ", version: " + buildVersion + ", build name: " + buildName;
    }
}
