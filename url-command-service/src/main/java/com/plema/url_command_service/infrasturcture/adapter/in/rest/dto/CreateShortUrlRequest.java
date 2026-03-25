package com.plema.url_command_service.infrasturcture.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
        @NotBlank(message = "originalUrl must not be blank")
        @Size(max = 255, message = "originalUrl must be at most 255 characters")
        String originalUrl
) {

}
