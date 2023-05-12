package com.myrecipe.service;

import com.myrecipe.entities.Comments;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.entities.responses.CommentsResponse;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.CommentsRepository;
import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyCommentService implements CommentsService{
    @Autowired
    RecipesRepository recipesRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    CommentsRepository commentsRepository;

    @Override
    public CommentsResponse createComment(CommentsRequest request) {
        Comments comment = new Comments();

        if (!request.getCommentDate().toString().isBlank()) {
            comment.setCommentDate(request.getCommentDate());
        } else
            throw new InvalidUserRequestException("Something went wrong while setting the date!");

        if (!request.getCommentText().isBlank()) {
            comment.setCommentText(request.getCommentText());
        } else
            throw new InvalidUserRequestException("Comment content is blank. Please write a comment.!");

        if (request.getRecipeId() != null) {
            comment.setRecipe(recipesRepository.getReferenceById(request.getRecipeId()));
        } else
            throw new InvalidUserRequestException("Recipe ID cannot be found!");

        if (request.getUserId() != null) {
            comment.setUser(usersRepository.getReferenceById(request.getUserId()));
        } else
            throw new InvalidUserRequestException("User ID cannot be found!");

        comment.setApproved(false);

        commentsRepository.save(comment);

        return new CommentsResponse("Comment added successfully");
    }

    @Override
    public List<Comments> getAllNonApprovedComments() {
        if(!commentsRepository.findNonApprovedComments().isEmpty())
            return commentsRepository.findNonApprovedComments();
        else
            throw new RecordNotFoundException("No comments waiting for approval found.");
    }
}
