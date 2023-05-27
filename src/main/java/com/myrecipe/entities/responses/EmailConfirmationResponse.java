package com.myrecipe.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailConfirmationResponse {
    private String message;
}
