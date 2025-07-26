package com.magicworld.tfg_angular_springboot.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
}
