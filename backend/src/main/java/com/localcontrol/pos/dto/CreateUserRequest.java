package com.localcontrol.pos.dto;

import com.localcontrol.pos.model.User;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private User.Role role;
}
