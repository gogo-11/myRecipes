package com.myrecipe.repository;

import com.myrecipe.entities.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Integer> {
    @Query(value = "SELECT * FROM comments WHERE is_approved = false", nativeQuery = true)
    List<Comments> findNonApprovedComments ();
}
