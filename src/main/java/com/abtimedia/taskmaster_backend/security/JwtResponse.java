package com.abtimedia.taskmaster_backend.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JwtResponse(@JsonProperty(value = "access_token") String accessToken) {
}
