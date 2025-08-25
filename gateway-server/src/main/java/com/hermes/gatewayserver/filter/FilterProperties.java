package com.hermes.gatewayserver.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class FilterProperties {
    
    private List<String> whitelist;
    private List<String> blacklist;
    private String secret;
    private long expirationTime;
    private long refreshExpiration;
    private Validation validation;
    private Management management;
    private Security security;

    @Getter
    @Setter
    public static class Validation {
        private String issuer;
        private String audience;
        private long clockSkew;
    }

    @Getter
    @Setter
    public static class Management {
        private Blacklist blacklist;
        private Refresh refresh;
    }

    @Getter
    @Setter
    public static class Blacklist {
        private boolean enabled;
        private long cleanupInterval;
    }

    @Getter
    @Setter
    public static class Refresh {
        private boolean rotationEnabled;
        private boolean reuseDetection;
        private int maxRefreshCount;
    }

    @Getter
    @Setter
    public static class Security {
        private boolean exposeToken;
        private boolean secureLogging;
    }
}