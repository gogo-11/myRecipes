package com.myrecipe.entities.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentsRequest {
    @NotNull
    @NotBlank
    private String commentText;

    @NotNull
    @NotBlank
    private LocalDateTime commentDate;

    @NotNull
    @NotBlank
    private boolean isApproved;

    @NotNull
    @NotBlank
    private Integer userId;

    @NotNull
    @NotBlank
    private Integer recipeId;
}
