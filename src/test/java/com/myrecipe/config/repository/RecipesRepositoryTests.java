package com.myrecipe.config.repository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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

    @Test
    public void findPublicRecipesReturnsPublicRecipesSortedByIdDescending() {
        Users author = userRepo.save(author("sorted-author@mail.com"));
        Recipes olderRecipe = recipe("Older recipe", false, author, Categories.MEAT);
        Recipes newerRecipe = recipe("Newer recipe", false, author, Categories.SOUPS);
        Recipes privateRecipe = recipe("Private sorted recipe", true, author, Categories.MEAT);
        recipeRepo.save(olderRecipe);
        recipeRepo.save(newerRecipe);
        recipeRepo.save(privateRecipe);
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).extracting(Recipes::getRecipeName)
                .containsExactly("Newer recipe", "Older recipe");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void findPublicRecipesFiltersByKeywordCaseInsensitive() {
        Users author = userRepo.save(author("keyword-author@mail.com"));
        recipeRepo.save(recipe("Chicken Soup", false, author, Categories.SOUPS));
        recipeRepo.save(recipe("Pancakes", false, author, Categories.DESSERTS));
        recipeRepo.save(recipe("Private chicken", true, author, Categories.MEAT));
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                "chicken",
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).extracting(Recipes::getRecipeName)
                .containsExactly("Chicken Soup");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findPublicRecipesFiltersByCategory() {
        Users author = userRepo.save(author("category-author@mail.com"));
        recipeRepo.save(recipe("Meat stew", false, author, Categories.MEAT));
        recipeRepo.save(recipe("Tomato soup", false, author, Categories.SOUPS));
        recipeRepo.save(recipe("Private soup", true, author, Categories.SOUPS));
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                null,
                Categories.SOUPS,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).extracting(Recipes::getRecipeName)
                .containsExactly("Tomato soup");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findPublicRecipesFiltersByKeywordAndCategory() {
        Users author = userRepo.save(author("combined-author@mail.com"));
        recipeRepo.save(recipe("Chicken soup", false, author, Categories.SOUPS));
        recipeRepo.save(recipe("Chicken salad", false, author, Categories.SALADS));
        recipeRepo.save(recipe("Vegetable soup", false, author, Categories.SOUPS));
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                "chicken",
                Categories.SOUPS,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).extracting(Recipes::getRecipeName)
                .containsExactly("Chicken soup");
        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void findPublicRecipesReturnsEmptyPageWhenNoMatches() {
        Users author = userRepo.save(author("no-match-author@mail.com"));
        recipeRepo.save(recipe("Existing recipe", false, author, Categories.MEATLESS));
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                "missing",
                Categories.SOUPS,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    public void findPublicRecipesAppliesPagination() {
        Users author = userRepo.save(author("pagination-author@mail.com"));
        recipeRepo.save(recipe("First paged recipe", false, author, Categories.MEAT));
        recipeRepo.save(recipe("Second paged recipe", false, author, Categories.MEAT));
        recipeRepo.save(recipe("Third paged recipe", false, author, Categories.MEAT));
        entityManager.flush();
        entityManager.clear();

        Page<Recipes> page = recipeRepo.findPublicRecipes(
                null,
                Categories.MEAT,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getContent()).extracting(Recipes::getRecipeName)
                .containsExactly("Second paged recipe");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(3);
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
        return recipe(recipeName, isPrivate, author, Categories.MEATLESS);
    }

    private Recipes recipe(String recipeName, Boolean isPrivate, Users author, Categories category) {
        Recipes recipe = new Recipes();
        recipe.setRecipeName(recipeName);
        recipe.setProducts("products");
        recipe.setPortions(4);
        recipe.setCookingTime(30);
        recipe.setCookingSteps("steps");
        recipe.setCategory(category);
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
