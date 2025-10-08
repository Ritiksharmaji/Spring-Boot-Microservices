package config_info.ConfifurationDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildInfoController {

//    @Value("${build.id}")
//    private String buildId;
//
//    @Value("${build.version}")
//    private String buildVersion;
//
//    @Value("${build.name}")
//    private String buildName;

    // other ways with default value it selph from here
//    @Value("${OS:default}")
//    private String buildId;
//
//    @Value("${USERPROFILE:default}")
//    private String buildVersion;
//
//    @Value("${JAVA_HOME:default}")
//    private String buildName;

    // commnad lne
    // Using default values if not provided externally
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
