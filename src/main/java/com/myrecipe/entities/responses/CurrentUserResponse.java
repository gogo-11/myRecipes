package com.myrecipe.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
