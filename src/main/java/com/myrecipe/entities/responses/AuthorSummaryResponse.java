package com.myrecipe.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorSummaryResponse {
    private Integer id;
    private String firstName;
    private String lastName;
}
