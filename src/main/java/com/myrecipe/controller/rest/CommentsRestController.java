package com.myrecipe.controller.rest;

import java.util.List;

import com.myrecipe.entities.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.entities.responses.CommentsResponse;
import com.myrecipe.service.CommentsService;

@RestController
@RequestMapping("/comments")
public class CommentsRestController {
    @Autowired
    CommentsService commentsService;

    @PostMapping()
    public ResponseEntity<CommentsResponse> addNewRecipe(@RequestBody CommentsRequest comment) {
        CommentsResponse newComment = commentsService.createComment(comment);
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @GetMapping("/waiting")
    public List<Comments> getNonApprovedComments() {
        return commentsService.getAllNonApprovedComments();
    }
}
