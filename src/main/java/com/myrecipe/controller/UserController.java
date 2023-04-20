package com.myrecipe.controller;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.myrecipe.entities.Categories;
import com.myrecipe.entities.Recipes;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.RecipesRequest;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.RecipesRepository;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.RecipesService;
import com.myrecipe.service.UsersService;
import org.apache.commons.collections4.ListUtils;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {
    @Autowired
    private UsersService usersService;

    @Autowired
    private RecipesService recipesService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RecipesRepository recipesRepo;

    @GetMapping({"/","/welcome"})
    public String homePage (Model model) {
        List<Recipes> recipesList = recipesService.getLastTenPublicRecipes();
        int partitionSize = 2;
        List<List<Recipes>> recipesRows = ListUtils.partition(recipesList, partitionSize);

        model.addAttribute("recipesRows", recipesRows);
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
    public String showRecipeById(@PathVariable("id") Integer id, Model model) {
        Recipes recipe = null;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/";
        }
        String products = recipe.getProducts().replace(',','\n');
        recipe.setProducts(products);

        if(Boolean.TRUE.equals(recipe.getIsPrivate())){
            return "redirect:/";
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
    public String addRecipe (@ModelAttribute(name="request") RecipesRequest request) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        String authentication = securityService.getAuthentication();
        Users user = usersService.getByEmail(authentication);

        request.setUserId(user.getId());
        recipesService.createRecipe(request, user.getId());

        return "redirect:/my-recipes";
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
            model.addAttribute("message", "Потърсете по категория или по име");
        }

        return "search";
    }

    @GetMapping("/delete-recipe/{id}")
    public String showDeletePage (@PathVariable("id") Integer id, Model model) {
        if (!securityService.isAuthenticated()) {
            return "redirect:/";
        }
        Recipes recipe = null;
        try{
            recipe = recipesService.getById(id);
        } catch (RecordNotFoundException e) {
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
        Users currentUser;
        currentUser = usersService.getByEmail(securityService.getAuthentication());

        String pass = req.getParameter("passwordConfirm");

        System.out.println(pass);
        System.out.println("gogogogogogogogogogogogogogogo");

        Users recipeOwner = recipesService.getById(id).getUser();

        if(!securityService.getAuthentication().equals(recipeOwner.getEmail())) {
            return "welcome";
        }

        if(!encoder.matches(pass, currentUser.getPassword())) {
            return "welcome";
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

        Users user;
        user = usersService.getByEmail(securityService.getAuthentication());

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

}
