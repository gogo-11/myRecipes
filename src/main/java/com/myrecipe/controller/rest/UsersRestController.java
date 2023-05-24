package com.myrecipe.controller.rest;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.UsersResponse;
import com.myrecipe.entities.Users;
import com.myrecipe.service.UsersService;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.service.ResetPasswordService;

@RestController
@RequestMapping("/user")
public class UsersRestController {
    @Autowired
    UsersService userService;
    @Autowired
    ResetPasswordService passwordService;
    @Autowired
    UsersRepository userRepo;

    /**
     * Method which returns a user found by id
     *
     * @param id is the id of the user we want to return
     * @return The user with the specified id from DB
     */
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserByID(@PathVariable("id") Integer id) {
        Users userByID = userService.getById(id);
        return new ResponseEntity<>(userByID, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UsersResponse> getByUserCredentials(@RequestBody UsersRequest usersRequest) {
        Users user = userService.getByUserCredentials(usersRequest);

        return new ResponseEntity<>(new UsersResponse("Login successful"), HttpStatus.OK);
    }

    /**
     * Creating new user
     *
     * @param user data entered by the user
     * @return Response for newly created user and HTTP status code for successful creation
     */
    @PostMapping("/register")
    public ResponseEntity<UsersResponse> addNewUser(@RequestBody UsersRequest user) {
        UsersResponse newUser = userService.createUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     *
     * @param id id of the user whose data is to be updated
     * @param usersRequest user input of the new data
     * @return updated user and HTTP status code
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Users> updatePartially(@PathVariable("id") Integer id, @RequestBody UsersRequest usersRequest){
        Optional<Users> user = userService.userUpdate(id, usersRequest);
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    /**
     *
     * @return confirmation for successful removal of the user and HTTP status code for successful operation
     */
    @DeleteMapping
    public ResponseEntity<String> deleteUser (@RequestBody UsersRequest usersRequest) {
        if(userService.getByUserCredentials(usersRequest) != null){
            userService.deleteUser(usersRequest);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User with the specified ID not found", HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/token/{id}")
    public ResponseEntity<String> deleteToken (@PathVariable("id") Integer id) {
        Users user = userService.getById(passwordService.getByTokenId(id).getUser().getId());
        user.setPasswordResetToken(null);
        this.userRepo.save(user);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }
}
