package com.example.thedeveloperlife.dto;
import lombok.Getter;

@Getter
public class UpdateRequestDto {
    private String nickname;
    private String password;
    private String checkPassword;
    private String email;
    private String introduce;
}
