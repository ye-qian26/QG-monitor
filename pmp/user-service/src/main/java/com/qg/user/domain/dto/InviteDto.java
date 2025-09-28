package com.qg.user.domain.dto;

import lombok.Data;

@Data
public class InviteDto {
    private Long userId;
    private String invitedCode;
}
