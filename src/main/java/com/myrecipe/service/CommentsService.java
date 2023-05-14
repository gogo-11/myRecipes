package com.myrecipe.service;

import com.myrecipe.entities.Comments;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.entities.responses.CommentsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface CommentsService {
    /**
     * @param request Comment information
     * @return Returns a response for either successful or unsuccessful comment creation
     */
    CommentsResponse createComment(CommentsRequest request);
    List<Comments> getAllNonApprovedComments();
    Optional<Comments> approveComment (Integer commentId, CommentsRequest request);
    Comments getById(Integer id);

    List<Comments> getByRecipe(Integer recipeId);
    void deleteComment(Integer id);
}
