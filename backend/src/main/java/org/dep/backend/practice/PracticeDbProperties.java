package org.dep.backend.practice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.practice")
public record PracticeDbProperties(String dbPath) {
}
