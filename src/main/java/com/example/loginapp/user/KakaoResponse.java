package com.example.loginapp.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

public class KakaoResponse {

    @Data
    public static class TokenDTO{
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("refresh_token")
        private String refreshToken;
        @JsonProperty("expires_in")
        private Integer expiresIn;
        private String scope;
        @JsonProperty("refresh_token_expires_in")
        private Integer refreshTokenExpiresIn;

        @Builder
        public TokenDTO(String accessToken, String tokenType, String refreshToken, Integer expiresIn, String scope, Integer refreshTokenExpiresIn) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.scope = scope;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        }
    }

    @Data
    public static class KakaoUserDTO{
        private Long id;
        @JsonProperty("connected_at")
        private String connectedAt;
        private Properties properties;

        public KakaoUserDTO(Long id, String connectedAt, Properties properties) {
            this.id = id;
            this.connectedAt = connectedAt;
            this.properties = properties;
        }

        @Data
        public static class Properties{
            private String nickname;
        }
    }
}
