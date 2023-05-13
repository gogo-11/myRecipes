package com.myrecipe.controller;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import com.myrecipe.entities.Comments;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.security.SecurityService;
import com.myrecipe.service.UsersService;

@Controller
public class AdminController {
    @Autowired
    private UsersService usersService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private PasswordEncoder encoder;

    /**
     * A method in the Admin controller which is used for displaying the page in the web browser
     *
     * @return Returns the template name
     */
    @GetMapping("admin-panel")
    public String showAdminPanel (Model model) {
        if(!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/";
        }

        model.addAttribute("admins", usersService.getAllAdminUsers());
        try{
            List<Comments> comList = commentsService.getAllNonApprovedComments();
            model.addAttribute("commentsToApprove", comList);
        } catch (RecordNotFoundException e) {
            System.out.println("No comments");
        }

        return "admin-panel";
    }

    @GetMapping("/new-admin")
    public String showAdminRegistration (Model model) {
        if (!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/";
        }

        return "new-admin";
    }

    @PostMapping("/new-admin")
    public String createAdmin (@ModelAttribute(name="request") UsersRequest request, BindingResult bindingResult, Model model) {
        if (!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "new-admin";
        }

        String emailRegexSqlInjection = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String nameRegex = "^[a-zA-Z'-]+$";

        request.setRole(RolesEn.ADMIN);

        if (!request.getFirstName().matches(nameRegex)) {
            model.addAttribute("errFirstName", "Invalid input. Първото Ви име може да съдържа само букви от латинската азбука!");
            return "new-admin";
        }

        if (!request.getLastName().matches(nameRegex)) {
            model.addAttribute("errLastName", "Invalid input. Фамилното Ви име може да съдържа само букви от латинската азбука!");
            return "new-admin";
        }

        if(!Pattern.compile(emailRegexPattern).matcher(request.getEmail()).matches() &&
                Pattern.compile(emailRegexSqlInjection).matcher(request.getEmail()).matches()) {
            model.addAttribute("errEmail", "Невалиден имейл!");
            return "new-admin";
        }

        if(request.getPassword().isBlank()) {
            model.addAttribute("errPass", "Моля въведете парола!");
            return "new-admin";
        }

        usersService.createUser(request);

        return "redirect:/admin-panel";
    }

    @GetMapping("/admin-panel/{id}")
    public String showEditAdminSection (@PathVariable Integer id, Model model) {
        if(!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/admin-panel";
        }
        Users admin;

        if(id != null) {
            try{
                admin = usersService.getById(id);
            } catch (RecordNotFoundException e) {
                return "redirect:/admin-panel";
            }

            if(!admin.getRole().equals(RolesEn.ADMIN)) {
                return "redirect:/admin-panel";
            }

            model.addAttribute("adminEdit", admin);
        }

        return "forward:/admin-panel";
    }

    @PostMapping("admin-panel/{id}")
    public String submitEditAdminForm (@ModelAttribute(name="request") UsersRequest request,
                                       @PathVariable("id") Integer id, BindingResult bindingResult, Model model) {
        if(!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/admin-panel";
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin-panel";
        }
        if(!usersService.getById(id).getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/admin-panel";
        }

        String emailRegexSqlInjection = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String emailRegexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        String nameRegex = "^[a-zA-Z'-]+$";

        request.setRole(RolesEn.ADMIN);
        model.addAttribute("admins", usersService.getAllAdminUsers());

        if (!request.getFirstName().matches(nameRegex)) {
            model.addAttribute("errFirstName", "Invalid input. Първото Ви име може да съдържа само букви от латинската азбука!");
            return "admin-panel";
        }

        if (!request.getLastName().matches(nameRegex)) {
            model.addAttribute("errLastName", "Invalid input. Фамилното Ви име може да съдържа само букви от латинската азбука!");
            return "admin-panel";
        }

        if(!Pattern.compile(emailRegexPattern).matcher(request.getEmail()).matches() ||
                !Pattern.compile(emailRegexSqlInjection).matcher(request.getEmail()).matches()) {
            model.addAttribute("errEmail", "Невалиден имейл!");
            return "admin-panel";
        }

        usersService.userUpdate(id, request);

        model.addAttribute("adminUpdated", "Информацията за администратора беше обновена успешно!!");
//        model.addAttribute("admins", usersService.getAllAdminUsers());

        return "admin-panel";
    }

    @GetMapping("admin-panel/delete/{id}")
    public String showDeleteAdminForm (@PathVariable Integer id, Model model) {
        if(!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/admin-panel";
        }

        if(usersService.getAllAdminUsers().size() <= 1){
            model.addAttribute("adminDeleted",
                    "Не можете да изтриете този администратор, тъй като няма други администратори!");
            return "admin-panel";
        }

        model.addAttribute("admins", usersService.getAllAdminUsers());
        model.addAttribute("adminToDelete", usersService.getById(id));
        return "admin-panel";
    }

    @PostMapping("/admin-panel/delete/{id}")
    public String deleteSelectedRecipe(@PathVariable("id") Integer id,  Model model,
                                       HttpServletRequest httpRequest) {
        if (!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/admin-panel";
        }

        if(usersService.getAllAdminUsers().size() <= 1){
            model.addAttribute("adminDeleted",
                    "Не можете да изтриете този администратор, тъй като няма други администратори!");
            return "admin-panel";
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        String pass = httpRequest.getParameter("passwordConfirm");

        System.out.println(pass);
        System.out.println("gogogogogogogogogogogogogogogo");

        if(!encoder.matches(pass, currentUser.getPassword())) {
            model.addAttribute("errorMessage", "Грешна парола!");
            model.addAttribute("adminToDelete", usersService.getById(id));
            return "admin-panel";
        }

        usersService.deleteAdminById(id);
        model.addAttribute("adminDeleted", "Администраторският профил е изтрит успешно!");

        return "redirect:/admin-panel";
    }

    @GetMapping("/admin-panel/approve-comment/{id}")
    public String approveCommentErrorCatch (@PathVariable("id") Integer id) {
        Comments comment;
        try{
            comment = commentsService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }
        return "redirect:/admin-panel";
    }

    @PostMapping("/admin-panel/approve-comment/{id}")
    public String approveComment (@PathVariable("id") Integer id, @ModelAttribute(name="request") CommentsRequest request, Model model) {
        if(!securityService.isAuthenticated() || !usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN)){
            return "redirect:/admin-panel";
        }
        Comments comment;
        try{
            comment = commentsService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }

        request.setApproved(true);
        request.setCommentText(comment.getCommentText());
        request.setCommentDate(comment.getCommentDate());
        request.setUserId(comment.getUser().getId());
        request.setRecipeId(comment.getRecipe().getId());
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("##################request date" + request.getCommentDate());
        System.out.println("##################now date" + LocalDateTime.now());

        model.addAttribute("admins", usersService.getAllAdminUsers());

        try{
            List<Comments> comList = commentsService.getAllNonApprovedComments();
            model.addAttribute("commentsToApprove", comList);
        } catch (RecordNotFoundException e) {
            System.out.println("No comments");
        }

        commentsService.approveComment(id, request);
        return "redirect:/admin-panel";
    }
}
