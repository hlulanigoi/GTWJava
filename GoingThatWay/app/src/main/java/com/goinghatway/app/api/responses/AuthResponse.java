package com.goinghatway.app.api.responses;

import com.google.gson.annotations.SerializedName;
import com.goinghatway.app.models.User;

public class AuthResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    public AuthResponse() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
