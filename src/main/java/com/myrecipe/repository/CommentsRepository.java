package com.myrecipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.Comments;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Integer> {
    @Query(value = "SELECT * FROM comments WHERE is_approved = false ORDER BY comment_id DESC", nativeQuery = true)
    List<Comments> findNonApprovedComments ();

    @Query(value = "SELECT * FROM comments WHERE recipe_id = :recipeId AND is_approved = true ORDER BY comment_date ASC", nativeQuery = true)
    List<Comments> findCommentsByRecipe (@Param("recipeId") Integer recipeId);
}
