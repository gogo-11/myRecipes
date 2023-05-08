package com.myrecipe.config.repository;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.UsersRepository;

@DataJpaTest
public class RecipesRepositoryTests {
    @Autowired
    private RecipesRepository recipeRepo;

    @Autowired
    private UsersRepository userRepo;

    @BeforeEach
    public void setup() {
        Users user = userRepo.save(new Users(
                1,
                "John",
                "Doe",
                "example@mail.com",
                "123456",
                RolesEn.USER,
                new ArrayList<>()));
    }

//    @Test
//    public void recipeRepositoryTest() {
//        Recipes rec = recipeRepo.save(new Recipes(
//                1,
//                "Name",
//                "pr",
//                1,
//                1,
//                "a",
//                Categories.MEAT,
//                false,
//                userRepo.findByEmail("example@mail.com")));
//
//        assertThat(rec).hasFieldOrPropertyWithValue("recipeName", "Name");
//        assertThat(rec).hasFieldOrPropertyWithValue("portions", 1);
//    }

//    @Test
//    public void findRecipeByNameTest () {
//        Recipes rec = recipeRepo.save(new Recipes(
//                1,
//                "Name",
//                "pr",
//                1,
//                1,
//                "a",
//                Categories.MEAT,
//                false,
//                userRepo.findByEmail("example@mail.com")));
//
//        Recipes recipeFound = recipeRepo.findByRecipeName("Name");
//
//        assertThat(recipeFound.equals(rec));
//    }

//    @Test
//    public void deleteAllRecipesTest () {
//        Recipes rec = recipeRepo.save(new Recipes(
//                1,
//                "Name",
//                "pr",
//                1,
//                1,
//                "a",
//                Categories.MEAT,
//                false,
//                userRepo.findByEmail("example@mail.com")));
//
//        Recipes rec2 = recipeRepo.save(new Recipes(
//                4,
//                "Name example",
//                "product, product",
//                4,
//                20,
//                "a",
//                Categories.MEATLESS,
//                false,
//                userRepo.findByEmail("example@mail.com")));
//
//        recipeRepo.deleteAll();
//
//        assertThat(recipeRepo.findAll()).isEmpty();
//    }
}
