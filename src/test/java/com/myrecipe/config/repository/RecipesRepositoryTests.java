package com.myrecipe.config.repository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

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

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void findPublicRecipeByIdReturnsPublicRecipeAndLoadsAuthor() {
        Users author = userRepo.save(author("public-author@mail.com"));
        Recipes publicRecipe = recipe("Public recipe", false, author);
        recipeRepo.save(publicRecipe);
        entityManager.flush();
        entityManager.clear();

        Optional<Recipes> foundRecipe = recipeRepo.findPublicRecipeById(publicRecipe.getId());

        assertThat(foundRecipe).isPresent();
        assertThat(foundRecipe.get().getId()).isEqualTo(publicRecipe.getId());
        assertThat(foundRecipe.get().getIsPrivate()).isFalse();
        assertThat(entityManager.getEntityManager().getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(foundRecipe.get().getUser())).isTrue();
        assertThat(foundRecipe.get().getUser().getEmail()).isEqualTo("public-author@mail.com");
    }

    @Test
    public void findPublicRecipeByIdDoesNotReturnPrivateRecipe() {
        Users author = userRepo.save(author("private-author@mail.com"));
        Recipes privateRecipe = recipe("Private recipe", true, author);
        recipeRepo.save(privateRecipe);
        entityManager.flush();
        entityManager.clear();

        Optional<Recipes> foundRecipe = recipeRepo.findPublicRecipeById(privateRecipe.getId());

        assertThat(foundRecipe).isEmpty();
    }

    private Users author(String email) {
        Users user = new Users();
        user.setFirstName("Author");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(RolesEn.USER);
        user.setActivated(true);
        return user;
    }

    private Recipes recipe(String recipeName, Boolean isPrivate, Users author) {
        Recipes recipe = new Recipes();
        recipe.setRecipeName(recipeName);
        recipe.setProducts("products");
        recipe.setPortions(4);
        recipe.setCookingTime(30);
        recipe.setCookingSteps("steps");
        recipe.setCategory(Categories.MEATLESS);
        recipe.setIsPrivate(isPrivate);
        recipe.setUser(author);
        return recipe;
    }

//    @BeforeEach
//    public void setup() {
//        Users user = userRepo.save(new Users(
//                1,
//                "John",
//                "Doe",
//                "example@mail.com",
//                "123456",
//                RolesEn.USER,
//                new ArrayList<>()));
//    }

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
