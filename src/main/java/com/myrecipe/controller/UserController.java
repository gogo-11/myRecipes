package com.myrecipe.controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.ListUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.RecipesService;
import com.myrecipe.service.UsersService;
import com.myrecipe.service.ImageUtils;

@Controller
public class UserController {
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    @Autowired
    private UsersService usersService;

    @Autowired
    private RecipesService recipesService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping({"/","/welcome"})
    public String homePage (Model model) {
        List<Recipes> recipesList = recipesService.getLastTenPublicRecipes();
        int partitionSize = 2;
        List<List<Recipes>> recipesRows = ListUtils.partition(recipesList, partitionSize);

        model.addAttribute("recipesRows", recipesRows);
        model.addAttribute("recipes", recipesList);

        return "welcome";
    }

    @GetMapping("/registration")
    public String showRegistration (Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }
//        model.addAttribute("request", new Users());

        return "registration";
    }

    @PostMapping("/registration")
    public String registration (@ModelAttribute(name="request") UsersRequest request, BindingResult bindingResult, Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        String emailRegexSqlInjection = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String nameRegex = "^[a-zA-Z'-]+$";

        request.setRole(RolesEn.USER);

        if (!request.getFirstName().matches(nameRegex)) {
            model.addAttribute("errFirstName", "Invalid input. Първото Ви име може да съдържа само букви от латинската азбука!");
            return "registration";
        }

        if (!request.getLastName().matches(nameRegex)) {
            model.addAttribute("errLastName", "Invalid input. Фамилното Ви име може да съдържа само букви от латинската азбука!");
            return "registration";
        }

        if(!Pattern.compile(emailRegexPattern).matcher(request.getEmail()).matches() &&
            Pattern.compile(emailRegexSqlInjection).matcher(request.getEmail()).matches()) {
            model.addAttribute("errEmail", "Невалиден имейл!");
            return "registration";
        }

        if(request.getPassword().isBlank()) {
            model.addAttribute("errPass", "Моля въведете парола!");
            return "registration";
        }

        usersService.createUser(request);
        securityService.autoLogin(request.getEmail(), request.getPassword());

        return "redirect:/welcome";
    }

    @GetMapping("/login_form")
    public String showLogin (Model model, String error, String logout) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (error != null)
            model.addAttribute("error", "Имейлът или паролата Ви са неправилни!");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login_form";
    }

    @GetMapping("/all-recipes/page/{pageNumber}")
    public String showAllRecipes(@PathVariable("pageNumber") int currentPage, Model model) {
        Page<Recipes> page = recipesService.getPage(currentPage);
        int totalPages = page.getTotalPages();
        long totalItems = page.getTotalElements();
        List<Recipes> recipes = page.getContent();

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("recipes", recipes);

        return "all-recipes";
    }

    @GetMapping("/all-recipes")
    public String getAllPages(Model model){
        String authentication = securityService.getAuthentication();
        model.addAttribute("userN", authentication);
        System.out.println("============== " + authentication);
        return showAllRecipes(1, model);
    }

    @GetMapping("/view-recipe/{id}")
    public String showRecipeById(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Recipes recipe = null;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }
        String valid = (String) session.getAttribute("valid");
        if(recipe.getIsPrivate() && (valid == null || valid.isBlank())) {
            return "redirect:/";
        }

        String products = recipe.getProducts().replace(',','\n');
//        String[] prodArr = recipe.getProducts().split(",");
//        List<String> prodList = Arrays.asList(prodArr);
        recipe.setProducts(products);

        if(Boolean.TRUE.equals(recipe.getIsPrivate())){
            if(!securityService.getAuthentication().equals(recipe.getUser().getEmail())){
                return "redirect:/";
            }
        }
        model.addAttribute("recipe", recipe);

        if(securityService.isAuthenticated()){
            String authentication = securityService.getAuthentication();
            model.addAttribute("user", usersService.getByEmail(authentication));
            model.addAttribute("userByRecipe", recipe.getUser().getEmail());
        }
        return "view-recipe";
    }

    @GetMapping("/add-recipe")
    public String showAddRecipe (Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("request", new Recipes());

        return "add-recipe";
    }

    @PostMapping("/add-recipe")
    public String addRecipe (@ModelAttribute(name="request") RecipesRequest request, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        String authentication = securityService.getAuthentication();
        Users user = usersService.getByEmail(authentication);

        if(request.getCategory() == null || request.getPortions() == null || request.getRecipeName().isBlank() ||
                request.getProducts().isBlank() || request.getCookingSteps().isBlank() || request.getCookingTime() == null) {
            model.addAttribute("errorMessage", "Попълнете всички полета!");
            return "add-recipe";
        }

        if (!imageFile.isEmpty()) {
            if (!imageFile.getContentType().equals("image/jpeg")) {
                model.addAttribute("errorMessage", "Моля изберете изображение с разширение \".jpg\"!");
                return "add-recipe";
            }
            try {
                byte[] imageBytes = ImageUtils.resizeImage(imageFile, MAX_WIDTH, MAX_HEIGHT);
                request.setImage(imageBytes);
            } catch (IOException e) {
                // Handle exception
                model.addAttribute("errorMessage", "Грешка при качването на изображение");
                return "add-recipe";
            }
        }

        request.setUserId(user.getId());
        recipesService.createRecipe(request, user.getId());

        return "redirect:/my-recipes";
    }

    @GetMapping("/recipes/image/{id}")
    public void renderImage(@PathVariable("id") Integer id, HttpServletResponse response) {
        Recipes recipe = recipesService.getById(id);
        if (recipe != null && recipe.getImage() != null) {
            try {
                response.setContentType("image/jpeg"); // Set the appropriate content type for your image
                response.getOutputStream().write(recipe.getImage());
                response.getOutputStream().flush();
            } catch (IOException e) {
                // Handle exception
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            }
        }
    }

    @GetMapping("/my-recipes")
    public String showMyRecipes (Model model, @ModelAttribute(name="recipeDeleted") String recipeDeleted) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        String authentication = securityService.getAuthentication();
        Users user = usersService.getByEmail(authentication);

        model.addAttribute("recipes", recipesService.getUsersAllPublicRecipes(user.getId()));
        return "my-recipes";
    }

    @GetMapping("/search")
    public String showSearchPage (Model model, String keyword, Categories cat) {
        if(keyword != null || cat != null) {
            if(keyword != null) {
                List<Recipes> recipesFound = recipesService.getByKeyword(keyword);
                if(!recipesFound.isEmpty()){
                    model.addAttribute("recipesFound", recipesFound);
                } else {
                    model.addAttribute("error", "Не бяха открити рецепти, съответстващи с вашето търсене!");
                }
            } else {
                RecipesRequest request = new RecipesRequest();
                request.setCategory(cat);
                List<Recipes> recipesFound = recipesService.getByCategory(request);
                if(!recipesFound.isEmpty()){
                    model.addAttribute("recipesFound", recipesFound);
                } else {
                    model.addAttribute("errorCat", "Не бяха открити рецепти в посочената категория!");
                }
            }
        } else {
            model.addAttribute("message", "Потърсете по категория или по ключова дума");
        }

        return "search";
    }

    @GetMapping("/delete-recipe/{id}")
    public String showDeletePage (@PathVariable("id") Integer id, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());
        Recipes recipe = null;

        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        if(!currentUser.getId().equals(recipe.getUser().getId()) && !currentUser.getRole().equals(RolesEn.ADMIN)){
            return "redirect:/";
        }
        model.addAttribute("recipe", recipe);

        model.addAttribute("request", new UsersRequest());


        return "delete-recipe";
    }

    @PostMapping("/delete/{id}")
    public String deleteSelectedRecipe(@PathVariable("id") Integer id,  Model model,
                                       HttpServletRequest req) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        String pass = req.getParameter("passwordConfirm");

        System.out.println(pass);
        System.out.println("gogogogogogogogogogogogogogogo");

        Users recipeOwner = recipesService.getById(id).getUser();

        if(!currentUser.getEmail().equals(recipeOwner.getEmail()) && !currentUser.getRole().equals(RolesEn.ADMIN)) {
            return "welcome";
        }

        if(!encoder.matches(pass, currentUser.getPassword())) {
            model.addAttribute("errorMessage", "Грешна парола!");
            model.addAttribute("recipe", recipesService.getById(id));
            return "delete-recipe";
        }

        recipesService.deleteRecipe(id);
        model.addAttribute("recipeDeleted", "Рецептата е изтрита успешно!");

        return "my-recipes";
    }

    @GetMapping("/account")
    public String showCurrentUserAccount(Model model){
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Users user = usersService.getByEmail(securityService.getAuthentication());

        model.addAttribute("currentUser", user);
        return "account";
    }

    @PostMapping("/account")
    public String updateUserAccount(@ModelAttribute(name="request") UsersRequest request, BindingResult bindingResult, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "account";
        }

        Users user = usersService.getByEmail(securityService.getAuthentication());

        String emailRegexSqlInjection = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String nameRegex = "^[a-zA-Z'-]+$";

        request.setRole(RolesEn.USER);

        if (!request.getFirstName().matches(nameRegex)) {
            model.addAttribute("errFirstName", "Invalid input. Първото Ви име може да съдържа само букви от латинската азбука!");
            return "account";
        }

        if (!request.getLastName().matches(nameRegex)) {
            model.addAttribute("errLastName", "Invalid input. Фамилното Ви име може да съдържа само букви от латинската азбука!");
            return "account";
        }

        if(!Pattern.compile(emailRegexPattern).matcher(request.getEmail()).matches() &&
                Pattern.compile(emailRegexSqlInjection).matcher(request.getEmail()).matches()) {
            model.addAttribute("errEmail", "Невалиден имейл!");
            return "account";
        }

        usersService.userUpdate(user.getId(), request);

        return "redirect:/account";
    }

    @GetMapping("/secret-recipes")
    public String showSecretRecipes(Model model, HttpSession session) {
        if(!securityService.isAuthenticated()){
            return "redirect:/welcome";
        }
        if(usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/welcome";
        }
        session.setAttribute("valid","Valid request");

        model.addAttribute("recipe", new Recipes());
        return "secret-recipes";
    }

    @PostMapping("/secret-recipes")
    public String viewPrivateRecipes(@RequestParam("password") String password, Model model, HttpSession session) {
        if(!securityService.isAuthenticated()){
            return "redirect:/welcome";
        }
        if(password.isBlank()){
            return "redirect:/welcome";
        }

        // Get the currently logged-in user
        Users loggedInUser = usersService.getByEmail(securityService.getAuthentication());

        UsersRequest currentUser = new UsersRequest();
        currentUser.setEmail(securityService.getAuthentication());
        currentUser.setPassword(password);

        if(loggedInUser.getRole().equals(RolesEn.ADMIN)){
            return "redirect:/welcome";
        }

        if(password.isBlank()) {
            model.addAttribute("errorMessage", "Моля въведете парола!");
            return "secret-recipes";
        }

        // Verify the entered password against the stored password for the logged-in user
        if (securityService.isAuthenticated() && encoder.matches(password, loggedInUser.getPassword())) {
            // If password is correct, retrieve private recipes from the database
            try{
                List<Recipes> privateRecipes = recipesService.getUsersAllPrivateRecipes(currentUser);
                model.addAttribute("recipes", privateRecipes);
            }
            catch (RecordNotFoundException e) {
                model.addAttribute("errorMessage", "Не сте добавили тайни рецепти.");
            }

            session.setAttribute("valid", "Valid request");

            // Add the list of private recipes to the model to be displayed in the view
        } else {
            // If password is incorrect, show error message
            model.addAttribute("errorMessage", "Грешна парола. Опитайте пак.");
        }
        return "secret-recipes";
    }

    @GetMapping("/edit-recipe/{id}")
    public String showEditRecipePage(@PathVariable("id") Integer id, Model model) {
        if(!securityService.isAuthenticated()){
            return "redirect:/welcome";
        }

        Recipes recipe = null;
        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        if(!currentUser.getId().equals(recipe.getUser().getId())){
            return "redirect:/";
        }

        model.addAttribute("recipe", recipesService.getById(id));

        return "edit-recipe";
    }

    @PostMapping("/edit-recipe/{id}")
    public String updateRecipeInfo(@ModelAttribute(name="request") RecipesRequest request,
                                   @PathVariable("id") Integer id, @RequestParam("imageFile") MultipartFile imageFile,
                                   BindingResult bindingResult, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "account";
        }

        Users user = usersService.getByEmail(securityService.getAuthentication());
        Recipes currentRecipe = recipesService.getById(id);

        model.addAttribute("recipe", currentRecipe);

        if(request.getCategory() == null || request.getPortions() == null || request.getRecipeName().isBlank() ||
                request.getProducts().isBlank() || request.getCookingSteps().isBlank() || request.getCookingTime() == null) {
            model.addAttribute("errorMessage", "Попълнете всички полета!");
            return "add-recipe";
        }

        if (!imageFile.isEmpty()) {
            if (!imageFile.getContentType().equals("image/jpeg") && !imageFile.getContentType().equals("image/jpg")) {
                model.addAttribute("errorMessage", "Моля изберете изображение с разширение \".jpg\"!");
                return "edit-recipe";
            }
            try {
                byte[] imageBytes = ImageUtils.resizeImage(imageFile, MAX_WIDTH, MAX_HEIGHT);
                request.setImage(imageBytes);
//                request.setImage(imageFile.getBytes());
            } catch (IOException e) {
                // Handle exception
                model.addAttribute("errorMessage", "Грешка при качването на изображение");
                return "edit-recipe";
            }
        } else {
            request.setImage(currentRecipe.getImage());
        }

        recipesService.recipeUpdate(id, request);

        return "redirect:/my-recipes";
    }
}
