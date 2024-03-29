package com.myrecipe.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myrecipe.entities.Recipes;

@Repository
public interface RecipesRepository extends JpaRepository<Recipes, Integer> {
    @Query(value = "SELECT * FROM recipes WHERE is_private = false", nativeQuery = true)
    Page<Recipes> findAllPublicRecipes(Pageable pageable);
    @Query(value = "SELECT * FROM recipes WHERE is_private = false", nativeQuery = true)
    List<Recipes> findAllPublicRecipes();

    @Query(value = "SELECT * FROM recipes WHERE is_private = true AND user_id = :userId", nativeQuery = true)
    List<Recipes> findUsersAllPrivateRecipes(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM recipes WHERE is_private = false AND user_id = :userId", nativeQuery = true)
    List<Recipes> findUsersAllPublicRecipes(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM recipes WHERE is_private = false AND recipe_name = :recipeName", nativeQuery = true)
    Recipes findByName(@Param("recipeName") String recipeName);

    @Query(value = "SELECT * FROM recipes WHERE is_private = false AND recipe_name LIKE %:keyword%", nativeQuery = true)
    List <Recipes> findByKeyword(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM recipes WHERE is_private = false AND category = :cat", nativeQuery = true)
    List<Recipes> findByCategory(@Param("cat") String cat);

    @Query(value= "SELECT * FROM recipes WHERE is_private = false ORDER BY recipe_id DESC LIMIT 10", nativeQuery = true)
    List<Recipes> findLastTenPublicRecipes();

    @Query(value = "SELECT * FROM recipes WHERE is_private = false AND category = :category ORDER BY rand() LIMIT 3", nativeQuery = true)
    List<Recipes> findRandomThreeByCategory(@Param("category") String category);
}
