package config_info.ConfifurationDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class BuildInfoController {
    @Value("${build.id:100}")
    private String buildId;

    @Value("${build.version:1.0.0}")
    private String buildVersion;

    @Value("${build.name:Default-Build}")
    private String buildName;

    @Value("${build.type:Default}")
    private String buildType;

    @GetMapping("/build-info")
    public String getBuildInfo() {
        return String.format("BuildId: %s | Version: %s | Name: %s | Type: %s",
                buildId, buildVersion, buildName, buildType);
    }
}