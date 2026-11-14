package com.app_ecom.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Fills @CreatedDate / @LastModifiedDate. Kept out of the main class so that
 * web-slice tests (@WebMvcTest) do not need a MongoDB mapping context.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
