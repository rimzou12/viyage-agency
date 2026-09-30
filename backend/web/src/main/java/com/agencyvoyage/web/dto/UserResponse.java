package com.agencyvoyage.web.dto;

import com.agencyvoyage.domain.user.User;

public record UserResponse(String id, String email, String displayName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id().toString(), user.email(), user.displayName());
    }
}
