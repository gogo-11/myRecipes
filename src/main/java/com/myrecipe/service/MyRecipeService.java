package com.myrecipe.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.ImageFormatException;
import com.myrecipe.exceptions.InvalidCategoryException;
import com.myrecipe.exceptions.InvalidLoginDataException;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.responses.RecipesResponse;
import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.entities.Categories;
import com.myrecipe.entities.requests.UsersRequest;

@Service
public class MyRecipeService implements RecipesService{
    private static final int MAX_WIDTH = 720;
    private static final int MAX_HEIGHT = 405;
    private static final int MIN_WIDTH = 700;
    private static final int MIN_HEIGHT = 400;
    @Autowired
    RecipesRepository recipesRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder encoder;

    /**
     *
     * @param recipesRequest data input by the user, which will be stored as new recipe
     * @param userId id of the user who will own the recipe
     * @return message for successfully created recipe
     */
    @Override
    public RecipesResponse createRecipe(RecipesRequest recipesRequest, Integer userId) {
        Users user = usersRepository.findById(userId).orElseThrow(() -> new RecordNotFoundException("User with the specified ID not found"));

        Recipes recipe = new Recipes();
        if (!recipesRequest.getRecipeName().isBlank()) {
            if (recipesRepository.findByName(recipesRequest.getRecipeName()) == null) {
                recipe.setRecipeName(recipesRequest.getRecipeName());
            } else
                throw new DuplicateRecordFoundException("A recipe with this name already exists.");
        } else
            throw new InvalidUserRequestException("Recipe name field in user's request is blank!");

        if (!recipesRequest.getProducts().isBlank()) {
            recipe.setProducts(recipesRequest.getProducts());
        } else
            throw new InvalidUserRequestException("Products field in user's request is blank!");

        if (recipesRequest.getPortions() != null) {
            recipe.setPortions(recipesRequest.getPortions());
        } else
            throw new InvalidUserRequestException("Portions field in user's request is blank!");

        if (recipesRequest.getCookingTime() != null) {
            recipe.setCookingTime(recipesRequest.getCookingTime());
        } else
            throw new InvalidUserRequestException("Cooking time field in user's request is blank!");

        if (!recipesRequest.getCookingSteps().isBlank()) {
            recipe.setCookingSteps(recipesRequest.getCookingSteps());
        } else
            throw new InvalidUserRequestException("Cooking steps field in user's request is blank!");

        if (Categories.categoryExists(recipesRequest.getCategory().toString())) {
            recipe.setCategory(recipesRequest.getCategory());
        } else
            throw new InvalidUserRequestException("Wrong category entered in category field in user's request!");

        if (recipesRequest.getIsPrivate() || !recipesRequest.getIsPrivate()) {
            recipe.setIsPrivate(recipesRequest.getIsPrivate());
        } else
            throw new InvalidUserRequestException("Incorrect input in field Is private in user's request!");

        if (recipesRequest.getImage() != null && !recipesRequest.getImage().isEmpty()) {
            recipe.setImage(setImageToRecipe(recipesRequest));
        }

        recipe.setUser(user);

        recipesRepository.save(recipe);
        return new RecipesResponse("Recipe created and stored successfully");
    }

    private byte[] setImageToRecipe (RecipesRequest request) {
        if (!request.getImage().getContentType().equals("image/jpeg")) {
            throw new ImageFormatException("Невалидно разширение на изображението. Разширението може да бъде само JPEG.");
        }
        byte[] imageBytes;
        try {
            if(!ImageUtils.isImageAboveMinSizeRange(request.getImage(),MIN_WIDTH,MIN_HEIGHT)) {
                throw new ImageFormatException("Изображението не съответства на минималните размери!");
            }
            imageBytes = ImageUtils.resizeImage(request.getImage(), MAX_WIDTH, MAX_HEIGHT);

        } catch (IOException e) {
            throw new ImageFormatException("Грешка при запазването на изображението!");
        }
        return imageBytes;
    }

    /**
     *
     * @param recipeId id of the recipe wanted for removal
     */
    @Override
    public void deleteRecipe(Integer recipeId) {
        if(recipesRepository.findById(recipeId).isPresent()) {
            recipesRepository.deleteById(recipeId);
        } else
            throw new RecordNotFoundException("Recipe with the specified ID does not exist!");
    }

    /**
     *
     * @param recipeId id of the recipe wanted
     * @return the wanted recipe
     */
    @Override
    public Recipes getById(Integer recipeId) {
        return recipesRepository
                .findById(recipeId)
                .orElseThrow(() -> new RecordNotFoundException("Recipe with the specified ID does not exist!"));
    }

    /**
     *
     * @param request recipe name which will be used for searching
     * @return the recipe with the same name if found
     */
    @Override
    public Recipes getByName(RecipesRequest request) {
        if(recipesRepository.findByName(request.getRecipeName()) != null) {
            return recipesRepository.findByName(request.getRecipeName());
        } else
            throw new RecordNotFoundException("No recipe with such name found!");
    }

    @Override
    public List<Recipes> getByKeyword(String keyword) {
        if(recipesRepository.findByKeyword(keyword) != null) {
            return recipesRepository.findByKeyword(keyword);
        } else
            throw new RecordNotFoundException("No recipes found!");
    }

    /**
     *
     * @param request recipe category which will be used for searching
     * @return list of recipes in that category
     */
    @Override
    public List<Recipes> getByCategory(RecipesRequest request) {

        if (Categories.categoryExists(request.getCategory().toString())){
            List<Recipes> recipes = recipesRepository.findByCategory(request.getCategory().toString());

            if (!recipes.isEmpty()) {
                return recipes;
            } else
                throw new RecordNotFoundException("No recipes found from the specified category!");
        } else {
            throw new InvalidCategoryException("Wrong category");
        }
    }

    @Override
    public List<Recipes> getRandomThreeByCategory(Integer recipeId, RecipesRequest request) {
        if (Categories.categoryExists(request.getCategory().toString())){
            List<Recipes> recipes = recipesRepository.findByCategory(request.getCategory().toString());

            recipes.removeIf(recipe -> recipe.getId().equals(recipeId));

            if (!recipes.isEmpty()) {
                return recipes;
            } else
                throw new RecordNotFoundException("No recipes found from the specified category!");
        } else {
            throw new InvalidCategoryException("Wrong category");
        }
    }

    /**
     *
     * @return list of all public recipes
     */
    @Override
    public List<Recipes> getAllPublicRecipes() {
        List<Recipes> publicRecipes = recipesRepository.findAllPublicRecipes();
        if (!publicRecipes.isEmpty()) {
            return publicRecipes;
        } else
            throw new RecordNotFoundException("No public recipes found!");
    }

    @Override
    public List<Recipes> getLastTenPublicRecipes() {
        List<Recipes> publicRecipes = recipesRepository.findLastTenPublicRecipes();
        if (!publicRecipes.isEmpty()) {
            return publicRecipes;
        } else
            throw new RecordNotFoundException("No public recipes found!");
    }

    /**
     *
     * @param usersRequest email and password of the user
     * @return list of the private recipes of the user
     */
    @Override
    public List<Recipes> getUsersAllPrivateRecipes(UsersRequest usersRequest) {
        Users user = usersRepository.findByEmail(usersRequest.getEmail());
        int userId = user.getId();

        if(user == null) {
            throw new InvalidLoginDataException("Wrong email or password!");
        }

        if(encoder.matches(usersRequest.getPassword(), user.getPassword()) && usersRequest.getEmail().equals(user.getEmail())) {

            List<Recipes> privateRecipes = recipesRepository.findUsersAllPrivateRecipes(userId);
            if (!privateRecipes.isEmpty()) {
                return privateRecipes;
            } else
                throw new RecordNotFoundException("This user does not have any private recipes");

        } else
            throw new SecurityException("Error accessing private recipes");
    }

    @Override
    public List<Recipes> getUsersAllPublicRecipes(Integer id) {
        Users user = usersRepository.getReferenceById(id);
        if(user == null) {
            throw new RecordNotFoundException("No such user!");
        }

        List<Recipes> userRecipes = recipesRepository.findUsersAllPublicRecipes(id);
        if (!userRecipes.isEmpty()) {
            return userRecipes;
        } else
            throw new RecordNotFoundException("This user does not have any public recipes");
    }

    @Override
    public Page<Recipes> getPage(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber-1,6);
        return recipesRepository.findAllPublicRecipes(pageable);
    }

    @Override
    public Optional<Recipes> recipeUpdate(Integer id, RecipesRequest request) {
        Optional<Recipes> recipe = recipesRepository.findById(id);

        if(recipe.isPresent()){
            if(request.getRecipeName().isBlank() || request.getRecipeName() == null){
                recipe.get().setRecipeName(recipesRepository.getReferenceById(id).getRecipeName());
            } else {
                recipe.get().setRecipeName(request.getRecipeName());
            }

            if(request.getPortions().toString().isBlank() || request.getPortions() == null){
                recipe.get().setPortions(recipesRepository.getReferenceById(id).getPortions());
            } else {
                recipe.get().setPortions(request.getPortions());
            }

            if(request.getCookingTime().toString().isBlank() || request.getCookingTime() == null){
                recipe.get().setCookingTime(recipesRepository.getReferenceById(id).getCookingTime());
            } else {
                recipe.get().setCookingTime(request.getCookingTime());
            }

            if(request.getCookingSteps().isBlank() || request.getCookingSteps() == null){
                recipe.get().setCookingSteps(recipesRepository.getReferenceById(id).getCookingSteps());
            } else {
                recipe.get().setCookingSteps(request.getCookingSteps());
            }

            if(request.getCategory().toString().isBlank() || request.getCategory() == null){
                recipe.get().setCategory(recipesRepository.getReferenceById(id).getCategory());
            } else {
                recipe.get().setCategory(request.getCategory());
            }

            if(request.getProducts().isBlank() || request.getProducts() == null){
                recipe.get().setProducts(recipesRepository.getReferenceById(id).getProducts());
            } else {
                recipe.get().setProducts(request.getProducts());
            }

            if(request.getImage().isEmpty() || request.getImage() == null){
                recipe.get().setImage(recipesRepository.getReferenceById(id).getImage());
            } else {
                recipe.get().setImage(setImageToRecipe(request));
            }

            recipe.get().setIsPrivate(request.getIsPrivate());

            recipesRepository.save(recipe.get());

            return recipe;
        } else {
            throw new RecordNotFoundException("User with the specified ID not found");
        }
    }
}
