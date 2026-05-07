package com.localcontrol.pos.dto;

import com.localcontrol.pos.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private User.Role role;
}
