package com.myrecipe.entities.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentsRequest {
    @NotNull
    @NotBlank(message = "Коментарът ви е празен")
    @Pattern(regexp = "[\\p{IsCyrillic}\\p{Punct}\\p{Zs}]+", message = "Необходимо е вашите коментари да са написани на кирилица!")
    private String commentText;

//    @NotNull
//    @NotBlank
    private LocalDateTime commentDate;

//    @NotNull
//    @NotBlank
    private boolean isApproved;

//    @NotNull
//    @NotBlank
    private Integer userId;

//    @NotNull
//    @NotBlank
    private Integer recipeId;
}
