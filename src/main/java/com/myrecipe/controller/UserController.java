package com.myrecipe.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.RecipesService;
import com.myrecipe.service.UsersService;
import com.myrecipe.service.CommentsService;
import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Comments;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.security.UserDetailsServiceImpl;
import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.ImageFormatException;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;

@Controller
public class UserController {
    private static final String CYRILLIC_NAME_REGEX = "[\\p{IsCyrillic}\\p{Zs}]+";
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RecipesService recipesService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private PasswordEncoder encoder;

    @GetMapping({"/","/welcome"})
    public String homePage (Model model) {
        try {
            recipesService.getLastTenPublicRecipes();
        } catch (RecordNotFoundException e) {
            model.addAttribute("error", true);
            return "welcome";
        }

        List<Recipes> recipesList = recipesService.getLastTenPublicRecipes();
        model.addAttribute("recipes", recipesList);

        return "welcome";
    }

    @GetMapping("/registration")
    public String showRegistration (Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("request", new UsersRequest());

        return "registration";
    }

    @PostMapping("/registration")
    public String registration (@Valid @ModelAttribute(name="request") UsersRequest request, BindingResult bindingResult, Model model) {
        if (securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        request.setRole(RolesEn.USER);
        request.setActivated(false);

        try {
            usersService.createUser(request);
        } catch (DuplicateRecordFoundException e) {
            model.addAttribute("error", "Вече е регистриран потребител с посочения имейл адрес!");
            return "registration";
        } catch (InvalidUserRequestException e) {
            model.addAttribute("error", "Попълнете всички полета!");
            return "registration";
        }

        return "forward:/send-confirmation-email";
    }

    @GetMapping("/login_form")
    public String showLogin (Model model, String error, String logout, HttpSession session) {
        if (securityService.isAuthenticated()) {
            if(usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)) {
                return "redirect:/admin-panel";
            }
            return "redirect:/";
        }

        String login = (String) session.getAttribute("login");
        model.addAttribute("login", login);
        session.setAttribute("login", null);

        if (error != null)
            model.addAttribute("error", "Въвели сте грешни данни или все още не сте потвърдили своя имейл!");

        if (logout != null)
            model.addAttribute("message", "Излязохте успешно.");

        return "login_form";
    }

    @PostMapping("/login_form")
    public String processLogin(@RequestParam("email") String email, Model model) {
        if (securityService.isAuthenticated()) {
            if(usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)) {
                return "redirect:/admin-panel";
            }
            return "redirect:/";
        }

        try {
            userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            model.addAttribute("errorMessage", "User not found");
            return "login_form";
        } catch (DisabledException e) {
            model.addAttribute("errorMessage", "not activated");
            return "login_form";
        }

        return "redirect:/welcome";
    }

    @GetMapping("/all-recipes/page/{pageNumber}")
    public String showAllRecipes(@PathVariable("pageNumber") String currentPage, Model model) {
        if(!isNumeric(currentPage)) {
            model.addAttribute("error", "Невалиден номер на страница");
            return "forward:/all-recipes";
        }
        int currentPageInt = Integer.parseInt(currentPage);
        try{
            recipesService.getPage(currentPageInt);
        } catch (RecordNotFoundException e) {
            model.addAttribute("errorMessage", true);
            return "redirect:/welcome";
        }
        if(currentPageInt <= 0) {
            return "redirect:/all-recipes";
        }

        Page<Recipes> page = recipesService.getPage(currentPageInt);
        int totalPages = page.getTotalPages();
        long totalItems = page.getTotalElements();
        List<Recipes> recipes = page.getContent();

        if(currentPageInt > totalPages) {
            return "redirect:/all-recipes/page/"+totalPages;
        }

        model.addAttribute("currentPage", currentPageInt);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("recipes", recipes);

        return "all-recipes";
    }

    @GetMapping("/all-recipes")
    public String getAllPages(Model model){

        return showAllRecipes(String.valueOf(1), model);
    }

    @GetMapping("/view-recipe/{id}")
    public String showRecipeById(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Recipes recipe;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }
        RecipesRequest request = new RecipesRequest();
        request.setCategory(recipe.getCategory());

        String validationError = (String) session.getAttribute("validationError");
        model.addAttribute("validationError", validationError);
        session.setAttribute("validationError", null);

        List<Recipes> threeRecipes = null;
        try {
            threeRecipes = recipesService.getRandomThreeByCategory(recipe.getId(), request);
        } catch (RecordNotFoundException e) {
            model.addAttribute("noSimilar", "Все още няма други рецепти от тази категория...");
        }

        model.addAttribute("recipes", threeRecipes);

        try{
            List<Comments> comList = commentsService.getByRecipe(id);
            model.addAttribute("recipeComments", comList);
        } catch (RecordNotFoundException e) {
            System.out.println("No comments");
        }

        String valid = (String) session.getAttribute("valid");
        if(Boolean.TRUE.equals(recipe.getIsPrivate()) && (valid == null || valid.isBlank())) {
            return "redirect:/";
        }
        session.setAttribute("valid", null);

        String approvalAwait = (String) session.getAttribute("approvalAwait");
        if (approvalAwait != null)
            if (!approvalAwait.isBlank()) {
                model.addAttribute("approvalAwait", approvalAwait);
            }
        session.setAttribute("approvalAwait", null);

        String commentDeleted = (String) session.getAttribute("commentDeleted");
        if (commentDeleted != null)
            if (!commentDeleted.isBlank()) {
                model.addAttribute("commentDeleted", commentDeleted);
            }
        session.setAttribute("commentDeleted", null);

//        List<String> recipeProducts = new ArrayList<>(Arrays.asList(recipe.getProducts().split("[ ,.]+")));
        List<String> recipeProducts = new ArrayList<>(Arrays.asList(recipe.getProducts().split(",")));
        model.addAttribute("recipeProducts", recipeProducts);

        if(Boolean.TRUE.equals(recipe.getIsPrivate())){
            if(!securityService.getAuthentication().equals(recipe.getUser().getEmail())){
                return "redirect:/";
            }
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("request", new CommentsRequest());

        if(securityService.isAuthenticated()){
            String authentication = securityService.getAuthentication();
            model.addAttribute("user", usersService.getByEmail(authentication));
            model.addAttribute("userByRecipe", recipe.getUser().getEmail());
        }
        return "view-recipe";
    }

    @GetMapping("/view-recipe/{id}/delete-comment/{commentId}")
    public String showCommentDeleteForm(@PathVariable("id") Integer id, @PathVariable("commentId") Integer commentId, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Recipes recipe;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/view-recipe/" + id;
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());
        Users recipeOwner = usersService.getById(recipe.getUser().getId());

        Comments commentToDelete;
        try{
            commentToDelete = commentsService.getById(commentId);
        } catch (RecordNotFoundException e) {
            return "redirect:/view-recipe/" + id;
        }

        if(!commentToDelete.getUser().equals(currentUser) && !recipeOwner.getId().equals(currentUser.getId())) {
            return "redirect:/";
        }

        model.addAttribute("commentToDelete", commentToDelete);

        model.addAttribute("showForm", true);
        return "forward:/view-recipe/" + id;
    }

    @PostMapping("/view-recipe/{id}/delete-comment/{commentId}")
    public String deleteComment (@PathVariable("id") Integer id, @PathVariable("commentId") Integer commentId, HttpSession session) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Recipes recipe;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/view-recipe/" + id;
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());
        Users recipeOwner = usersService.getById(recipe.getUser().getId());

        Comments commentToDelete;
        try{
            commentToDelete = commentsService.getById(commentId);
        } catch (RecordNotFoundException e) {
            return "redirect:/view-recipe/" + id;
        }

        if(!commentToDelete.getUser().equals(currentUser) && !recipeOwner.getId().equals(currentUser.getId())) {
            return "redirect:/view-recipe/" + id;
        }
        session.setAttribute("commentDeleted", "Коментарът е изтрит!");

        commentsService.deleteComment(commentId);
        return "redirect:/view-recipe/" + id;
    }

    @PostMapping("/view-recipe/{id}/add-comment")
    public String addNewComment(@PathVariable("id") Integer id,
                                @Valid @ModelAttribute(name="request") CommentsRequest request,
                                BindingResult result, HttpSession session) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if(result.hasErrors()){
            session.setAttribute("validationError", result.getFieldError("commentText").getDefaultMessage());
            return "redirect:/view-recipe/"+id;
        }

        try{
            recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        request.setApproved(false);
        request.setRecipeId(id);
        request.setUserId(usersService.getByEmail(securityService.getAuthentication()).getId());
        request.setCommentDate(LocalDateTime.now());

        commentsService.createComment(request);
        session.setAttribute("approvalAwait", "Вашият коментар е в процес на преглед. Благодарим за търпението!");
        return "redirect:/view-recipe/{id}";
    }

    @GetMapping("/add-recipe")
    public String showAddRecipe (Model model, HttpSession session) {
        if (!securityService.isAuthenticated()) {
            session.setAttribute("login", "Трябва да влезете в акаунта си, за да създадете рецепта!");
            return "forward:/login_form";
        }
        if(!isUser())
            return "redirect:/";
        model.addAttribute("request", new RecipesRequest());

        return "add-recipe";
    }

    @PostMapping("/add-recipe")
    public String addRecipe (@Valid @ModelAttribute(name="request") RecipesRequest request,
                             BindingResult result, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/login_form";
        }

        if(!isUser()) {
            return "redirect:/";
        }

        if(result.hasErrors()) {
            return "add-recipe";
        }

        String authentication = securityService.getAuthentication();
        Users user = usersService.getByEmail(authentication);

        if(request.getCategory().toString().isBlank() || request.getPortions().toString().isBlank()|| request.getRecipeName().isBlank() ||
                request.getProducts().isBlank() || request.getCookingSteps().isBlank() || request.getCookingTime().toString().isBlank()) {
            model.addAttribute("errorMessage", "Попълнете всички полета!");
            return "add-recipe";
        }

        request.setUserId(user.getId());

        try {
            recipesService.createRecipe(request, user.getId());
        } catch (DuplicateRecordFoundException e) {
            model.addAttribute("errorMessage", "Вече има рецепта със същото име");
            return "add-recipe";
        } catch (InvalidUserRequestException e) {
            model.addAttribute("errorMessage", "Попълнете правилно всички полета");
            return "add-recipe";
        } catch (ImageFormatException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "add-recipe";
        }

        return "redirect:/my-recipes";
    }

    @GetMapping("/recipes/image/{id}")
    public void renderImage(@PathVariable("id") Integer id, HttpServletResponse response) {
        Recipes recipe = recipesService.getById(id);
        if (recipe != null && recipe.getImage() != null) {
            try {
                response.setContentType("image/jpeg");
                response.getOutputStream().write(recipe.getImage());
                response.getOutputStream().flush();
            } catch (IOException e) {
                System.out.println("Проблем при връщане на изображение!");
            }
        }
    }

    @GetMapping("/my-recipes")
    public String showMyRecipes (Model model, @ModelAttribute(name="recipeDeleted") String recipeDeleted) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if(!isUser()) {
            return "redirect:/";
        }

        String authentication = securityService.getAuthentication();
        Users user = usersService.getByEmail(authentication);

        try{
            recipesService.getUsersAllPublicRecipes(user.getId());
        } catch (RecordNotFoundException e){
            model.addAttribute("error", "Все още нямате добавени рецепти");
            return "my-recipes";
        }

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
                List<Recipes> recipesFound;
                try{
                    recipesFound = recipesService.getByCategory(request);
                } catch (RecordNotFoundException e) {
                    model.addAttribute("errorCat", "Не бяха открити рецепти в посочената категория!");
                    return "search";
                }

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
        Recipes recipe;

        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        if(!currentUser.getId().equals(recipe.getUser().getId()) && !currentUser.getRole().equals(RolesEn.ADMIN)){
            return "redirect:/";
        }
        model.addAttribute("recipe", recipe);

        return "delete-recipe";
    }

    @PostMapping("/delete/{id}")
    public String deleteSelectedRecipe(@PathVariable("id") Integer id,  Model model,
                                       HttpServletRequest req) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }

        Recipes recipe;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        String pass = req.getParameter("passwordConfirm");

        Users recipeOwner = recipe.getUser();

        if(!currentUser.getEmail().equals(recipeOwner.getEmail()) && !currentUser.getRole().equals(RolesEn.ADMIN)) {
            return "welcome";
        }

        if(!encoder.matches(pass, currentUser.getPassword())) {
            model.addAttribute("errorMessage", "Грешна парола!");
            model.addAttribute("recipe", recipe);
            return "delete-recipe";
        }
        try{
            recipesService.deleteRecipe(id);
        } catch (RecordNotFoundException e) {
            model.addAttribute("error", "Не е открита рецепта с посоченият ID номер!");
            return "my-recipes";
        }

        model.addAttribute("recipeDeleted", "Рецептата е изтрита успешно!");

        return "my-recipes";
    }

    @GetMapping("/account")
    public String showCurrentUserAccount(Model model){
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if(!isUser())
            return "redirect:/";

        Users user = usersService.getByEmail(securityService.getAuthentication());

        model.addAttribute("currentUser", user);
        return "account";
    }

    @PostMapping("/account")
    public String updateUserAccount(@RequestParam("oldPass") String oldPass,
                                    @ModelAttribute(name="request") UsersRequest request,
                                    BindingResult bindingResult, Model model) {

        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "account";
        }
        if(!isUser())
            return "redirect:/";

        Users user = usersService.getByEmail(securityService.getAuthentication());
        model.addAttribute("currentUser", user);


        request.setRole(RolesEn.USER);

        if(!request.getFirstName().isBlank() || request.getFirstName() != null) {
            if (!request.getFirstName().matches(CYRILLIC_NAME_REGEX)) {
                model.addAttribute("errFirstName", "Първото Ви име трябва да е написано на кирилица!");
                return "account";
            }
        }

        if(!request.getLastName().isBlank() || request.getLastName() != null) {
            if (!request.getLastName().matches(CYRILLIC_NAME_REGEX)) {
                model.addAttribute("errLastName", "Фамилното Ви име трябва да е написано на кирилица!");
                return "account";
            }
        }

        if(oldPass.isBlank()) {
            model.addAttribute("errPass", "Невалидна парола!");
            model.addAttribute("currentUser", user);
            return "account";
        }

        if (!encoder.matches(oldPass, user.getPassword())) {
            model.addAttribute("errPass", "Грешна парола!");
            model.addAttribute("currentUser", user);
            return "account";
        }

        try {
            usersService.userUpdate(user.getId(), request);
        } catch (RecordNotFoundException e) {
            model.addAttribute("errPass", "Не беше намерен потребител за актуализиране");
            return "account";
        }

        model.addAttribute("userUpdated", "Профилът Ви беше успешно актуализиран!");

        return "account";
    }

    @GetMapping("/secret-recipes")
    public String showSecretRecipes(Model model, HttpSession session) {
        if(!securityService.isAuthenticated()){
            return "redirect:/welcome";
        }
        if(!isUser())
            return "redirect:/";

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

        Users loggedInUser = usersService.getByEmail(securityService.getAuthentication());

        UsersRequest currentUser = new UsersRequest();
        currentUser.setEmail(securityService.getAuthentication());
        currentUser.setPassword(password);

        if(!isUser())
            return "redirect:/";

        if(password.isBlank()) {
            model.addAttribute("errorMessage", "Моля въведете парола!");
            return "secret-recipes";
        }

        if (securityService.isAuthenticated() && encoder.matches(password, loggedInUser.getPassword())) {
            try{
                List<Recipes> privateRecipes = recipesService.getUsersAllPrivateRecipes(currentUser);
                model.addAttribute("recipes", privateRecipes);
            } catch (RecordNotFoundException e) {
                model.addAttribute("errorMessage", "Не сте добавили тайни рецепти.");
                return "secret-recipes";
            }

            session.setAttribute("valid", "Valid request");

        } else {
            model.addAttribute("errorMessage", "Грешна парола. Опитайте пак.");
        }
        return "secret-recipes";
    }

    @GetMapping("/edit-recipe/{id}")
    public String showEditRecipePage(@PathVariable("id") Integer id, Model model) {
        if(!securityService.isAuthenticated()){
            return "redirect:/view-recipe/" + id;
        }
        if(!isUser()) {
            return "redirect:/";
        }

        Recipes recipe;
        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        if(!currentUser.getId().equals(recipe.getUser().getId())){
            return "redirect:/view-recipe/" + id;
        }
        RecipesRequest request = new RecipesRequest();
        request.setUserId(recipe.getUser().getId());
        request.setRecipeName(recipe.getRecipeName());
        request.setPortions(recipe.getPortions());
        request.setProducts(recipe.getProducts());
        request.setCookingSteps(recipe.getCookingSteps());
        request.setCookingTime(recipe.getCookingTime());
        request.setIsPrivate(recipe.getIsPrivate());
        request.setCategory(recipe.getCategory());


        model.addAttribute("recipe", recipe);
        model.addAttribute("request", request);

        return "edit-recipe";
    }

    @PostMapping("/edit-recipe/{id}")
    public String updateRecipeInfo(@Valid @ModelAttribute(name="request") RecipesRequest request, BindingResult bindingResult,
                                   @PathVariable("id") Integer id,  Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        if(!isUser()) {
            return "redirect:/";
        }

        Users user = usersService.getByEmail(securityService.getAuthentication());
        Recipes currentRecipe = recipesService.getById(id);
        model.addAttribute("recipe", currentRecipe);

        if (bindingResult.hasFieldErrors()) {
            return "edit-recipe";
        }

        try{
            recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }

        if(request.getCategory().toString().isEmpty() || request.getPortions().toString().isBlank() || request.getRecipeName().isBlank() ||
                request.getProducts().isBlank() || request.getCookingSteps().isBlank() || request.getCookingTime().toString().isBlank()) {
            model.addAttribute("errorMessage", "Попълнете всички полета!");
            return "add-recipe";
        }

        request.setUserId(user.getId());

        recipesService.recipeUpdate(id, request);

        return "redirect:/view-recipe/"+id;
    }

    @GetMapping("/account/delete")
    public String showDeleteAccountConfirmation (Model model) {
        if(!securityService.isAuthenticated()){
            return "redirect:/login_form";
        }

        if(!isUser()) {
            return "redirect:/";
        }

        model.addAttribute("showDelete", "Show delete confirmation form");
        return "forward:/account";
    }

    @PostMapping("account/delete")
    public String deleteUserProfile (@ModelAttribute("request") UsersRequest request, Model model, HttpSession session) {
        if(!securityService.isAuthenticated()){
            return "redirect:/login_form";
        }

        if(!isUser()) {
            return "redirect:/";
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());
        request.setEmail(securityService.getAuthentication());



        if(!encoder.matches(request.getPassword(),currentUser.getPassword())) {
            model.addAttribute("error", "Неправилна парола!");
            Users user = usersService.getByEmail(securityService.getAuthentication());
            model.addAttribute("currentUser", user);
            model.addAttribute("showDelete", "Show delete confirmation form");
            return "account";
        }

        usersService.deleteUser(request);
        session.invalidate();
        return "redirect:/";
    }

    private boolean isUser() {
        if(securityService.isAuthenticated()){
            return usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.USER);
        } else
            return  false;
    }

    public boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
