package com.myrecipe.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.myrecipe.entities.Comments;
import com.myrecipe.entities.requests.CommentsRequest;
import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
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
    private static final String CYRILLIC_NAME_REGEX = "[\\p{IsCyrillic}\\p{Zs}]+";
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
    @GetMapping("/admin-panel")
    public String showAdminPanel (Model model, HttpSession session) {
        if(!securityService.isAuthenticated() || !isAdmin()){
            return "redirect:/";
        }

        model.addAttribute("admins", usersService.getAllAdminUsers());

        try{
            List<Comments> comList = commentsService.getAllNonApprovedComments();
            model.addAttribute("commentsToApprove", comList);
        } catch (RecordNotFoundException e) {
            System.out.println("No comments");
        }

        String commentApproved = (String) session.getAttribute("commentApproved");
        if (commentApproved != null)
            if (!commentApproved.isBlank()) {
                model.addAttribute("commentApproved", commentApproved);
            }

        String commentDeleted = (String) session.getAttribute("commentDeleted");
        if (commentDeleted != null)
            if (!commentDeleted.isBlank()) {
                model.addAttribute("commentDeleted", commentDeleted);
            }

        session.setAttribute("commentApproved", null);
        session.setAttribute("commentDeleted", null);

        return "admin-panel";
    }

    @GetMapping("/new-admin")
    public String showAdminRegistration (Model model) {
        if (!securityService.isAuthenticated() || !isAdmin()) {
            return "redirect:/";
        }

        model.addAttribute("request", new UsersRequest());

        return "new-admin";
    }

    @PostMapping("/new-admin")
    public String createAdmin (@Valid @ModelAttribute(name="request") UsersRequest request, BindingResult bindingResult, Model model) {
        if (!securityService.isAuthenticated() || !isAdmin()) {
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "new-admin";
        }

        request.setRole(RolesEn.ADMIN);

        try {
            usersService.createUser(request);
        } catch (DuplicateRecordFoundException e) {
            model.addAttribute("error", "Вече е регистриран потребител с посочения имейл адрес!");
            return "new-admin";
        } catch (InvalidUserRequestException e) {
            model.addAttribute("error", "Попълнете всички полета!");
            return "new-admin";
        }

        return "redirect:/admin-panel";
    }

    @GetMapping("/admin-panel/{id}")
    public String showEditAdminSection (@PathVariable Integer id, Model model) {
        if(!securityService.isAuthenticated() || !isAdmin()){
            return "redirect:/";
        }

        Users admin;

        if(id != null && id > 0) {
            try{
                admin = usersService.getById(id);
            } catch (RecordNotFoundException e) {
                return "redirect:/admin-panel";
            }

            if(!admin.getRole().equals(RolesEn.ADMIN)) {
                return "redirect:/admin-panel";
            }

            model.addAttribute("adminEdit", admin);
        } else {
            return "admin-panel";
        }

        return "forward:/admin-panel";
    }

    @PostMapping("/admin-panel/{id}")
    public String submitEditAdminForm (@ModelAttribute(name="request") UsersRequest request,
                                       @PathVariable("id") Integer id, BindingResult bindingResult, Model model) {
        if(!securityService.isAuthenticated() || !isAdmin()){
            return "redirect:/";
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin-panel";
        }
        if(!usersService.getById(id).getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/admin-panel";
        }

        request.setRole(RolesEn.ADMIN);
        model.addAttribute("admins", usersService.getAllAdminUsers());

        if (!request.getFirstName().isBlank() || request.getFirstName() != null) {
            if (!request.getFirstName().matches(CYRILLIC_NAME_REGEX)) {
                model.addAttribute("error", "Първото Ви име трябва да е написано на кирилица!");
                return "admin-panel";
            }
        }

        if (!request.getLastName().isBlank() || request.getLastName() != null) {
            if (!request.getLastName().matches(CYRILLIC_NAME_REGEX)) {
                model.addAttribute("error", "Фамилното Ви име трябва да е написано на кирилица!");
                return "admin-panel";
            }
        }

        usersService.userUpdate(id, request);

        model.addAttribute("adminUpdated", "Информацията за администратора беше обновена успешно!!");

        return "admin-panel";
    }

    @GetMapping("/admin-panel/delete/{id}")
    public String showDeleteAdminForm (@PathVariable Integer id, Model model) {
        if(!securityService.isAuthenticated() || !isAdmin()){
            return "redirect:/";
        }

        if(usersService.getAllAdminUsers().size() <= 1){
            model.addAttribute("adminDeleted",
                    "Не можете да изтриете този администратор, тъй като няма други администратори!");
            return "admin-panel";
        }

        Users user;
        try {
            user = usersService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }

        if(!user.getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/admin-panel";
        }

        model.addAttribute("admins", usersService.getAllAdminUsers());
        model.addAttribute("adminToDelete", usersService.getById(id));
        return "admin-panel";
    }

    @PostMapping("/admin-panel/delete/{id}")
    public String deleteSelectedRecipe(@PathVariable("id") Integer id,  Model model,
                                       HttpServletRequest httpRequest, HttpSession session) {
        if (!securityService.isAuthenticated() || !isAdmin()) {
            return "redirect:/";
        }

        if(usersService.getAllAdminUsers().size() <= 1){
            model.addAttribute("adminDeleted",
                    "Не можете да изтриете този администратор, тъй като няма други администратори!");
            return "admin-panel";
        }

        Users user;
        try {
            user = usersService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }

        if(!user.getRole().equals(RolesEn.ADMIN)) {
            return "redirect:/admin-panel";
        }

        Users currentUser = usersService.getByEmail(securityService.getAuthentication());

        String pass = httpRequest.getParameter("passwordConfirm");

        if(!encoder.matches(pass, currentUser.getPassword()) || pass.isBlank()) {
            model.addAttribute("errorMessage", "Грешна парола!");
            model.addAttribute("adminToDelete", usersService.getById(id));
            return "admin-panel";
        }

        usersService.deleteAdminById(id);
        model.addAttribute("adminDeleted", "Администраторският профил е изтрит успешно!");
        if(Objects.equals(id, currentUser.getId())) {
            session.invalidate();
        }

        return "redirect:/admin-panel";
    }

    @GetMapping("/admin-panel/approve-comment/{id}")
    public String approveCommentErrorCatch (@PathVariable("id") Integer id) {
        if (!securityService.isAuthenticated() || !isAdmin()) {
            return "redirect:/";
        }

        try{
            commentsService.getById(id);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }
        return "redirect:/admin-panel";
    }

    @PostMapping("/admin-panel/approve-comment/{id}")
    public String approveComment (@PathVariable("id") Integer id,
                                  @ModelAttribute(name="request") CommentsRequest request, Model model, HttpSession session) {
        if(!securityService.isAuthenticated() || !isAdmin()){
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

        model.addAttribute("admins", usersService.getAllAdminUsers());

        try{
            List<Comments> comList = commentsService.getAllNonApprovedComments();
            model.addAttribute("commentsToApprove", comList);
        } catch (RecordNotFoundException e) {
            System.out.println("No comments");
        }

        session.setAttribute("commentApproved", "Коментарът е одобрен!");

        commentsService.approveComment(id, request);
        return "redirect:/admin-panel";
    }

    @PostMapping("/admin-panel/delete-comment/{id}")
    public String deleteCommentOnApproval (@PathVariable("id") Integer commentId, HttpSession session) {
        if(!securityService.isAuthenticated() || !isAdmin()){
            return "redirect:/admin-panel";
        }
        Comments comment;
        try{
            comment = commentsService.getById(commentId);
        } catch (RecordNotFoundException e) {
            return "redirect:/admin-panel";
        }

        session.setAttribute("commentDeleted", "Коментарът е изтрит!");

        commentsService.deleteComment(commentId);
        return "redirect:/admin-panel";
    }

    private boolean isAdmin() {
        if(securityService.isAuthenticated()){
            return usersService.getByEmail(securityService.getAuthentication()).getRole().equals(RolesEn.ADMIN);
        } else
            return  false;
    }
}
