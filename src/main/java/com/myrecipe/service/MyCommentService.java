package com.myrecipe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myrecipe.entities.Comments;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.entities.responses.CommentsResponse;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.CommentsRepository;
import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.repository.UsersRepository;

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

    @Override
    public Optional<Comments> approveComment(Integer commentId, CommentsRequest request) {
        Optional<Comments> commentToApprove = commentsRepository.findById(commentId);
        if(commentToApprove.isPresent()) {
            request.setApproved(true);

            if(request.getUserId().toString().isBlank()) {
                commentToApprove.get().setUser(commentToApprove.get().getUser());
            } else {
                commentToApprove.get().setUser(usersRepository.getReferenceById(request.getUserId()));
            }

            if(request.getRecipeId().toString().isBlank()) {
                commentToApprove.get().setRecipe(commentToApprove.get().getRecipe());
            } else {
                commentToApprove.get().setRecipe(recipesRepository.getReferenceById(request.getRecipeId()));
            }

            if(request.getCommentText().isBlank()) {
                commentToApprove.get().setCommentText(commentToApprove.get().getCommentText());
            } else {
                commentToApprove.get().setCommentText(request.getCommentText());
            }

            if(request.getCommentDate().toString().isBlank() || request.getCommentDate() == null) {
                commentToApprove.get().setCommentDate(commentToApprove.get().getCommentDate());
            } else {
                commentToApprove.get().setCommentDate(request.getCommentDate());
            }

            commentToApprove.get().setApproved(request.isApproved());
            commentsRepository.save(commentToApprove.get());
            return commentToApprove;
        } else
            throw new RecordNotFoundException("No recipe with such ID found!");
    }

    @Override
    public Comments getById(Integer id) {
        Optional<Comments> comment = commentsRepository.findById(id);
        if(comment.isPresent()) {
            return comment.get();
        } else
            throw new RecordNotFoundException("No recipe with such ID found");
    }

    @Override
    public List<Comments> getByRecipe(Integer recipeId) {
        List<Comments> comments = commentsRepository.findCommentsByRecipe(recipeId);
        if(!comments.isEmpty()) {
            return comments;
        } else {
            throw new RecordNotFoundException("No comments found for this recipe");
        }

    }

    @Override
    public void deleteComment(Integer id) {
        if(commentsRepository.existsById(id)){
            commentsRepository.delete(commentsRepository.getReferenceById(id));
        } else
            throw new RecordNotFoundException("No comment found under the specified id");
    }
}
