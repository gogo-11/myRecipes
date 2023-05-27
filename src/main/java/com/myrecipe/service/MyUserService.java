package com.myrecipe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.myrecipe.entities.requests.UsersRequest;
import com.myrecipe.entities.responses.UsersResponse;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.UsersRepository;
import com.myrecipe.entities.RolesEn;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.DuplicateRecordFoundException;
import com.myrecipe.exceptions.InvalidLoginDataException;
import com.myrecipe.exceptions.RecordNotFoundException;

@Service
public class MyUserService implements UsersService{
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    /**
     *
     * @param userRequest object with the user info, entered by the user
     * @return response for successful creation of user
     */
    @Override
    public UsersResponse createUser(UsersRequest userRequest){
        Users user = new Users();

        if(!userRequest.getFirstName().isBlank()) {
            user.setFirstName(userRequest.getFirstName());
        } else
            throw new InvalidUserRequestException("First name field in user's request is blank!");

        if(!userRequest.getLastName().isBlank()) {
            user.setLastName(userRequest.getLastName());
        } else
            throw new InvalidUserRequestException("Last name field in user's request is blank!");

        if(!userRequest.getEmail().isBlank()) {
            if(userRepository.findByEmail(userRequest.getEmail()) == null) {
                user.setEmail(userRequest.getEmail());
            } else
                throw new DuplicateRecordFoundException("An account with this email already exists!");

        } else
            throw new InvalidUserRequestException("Email field in user's request is blank!");

        if(!userRequest.getPassword().isBlank()) {
            user.setPassword(encoder.encode(userRequest.getPassword()));
            System.out.println(encoder.matches(userRequest.getPassword(), user.getPassword()));

        } else
            throw new InvalidUserRequestException("Password field in user's request is blank!");

        if(userRequest.getRole().equals(RolesEn.USER) || userRequest.getRole().equals(RolesEn.ADMIN)) {
            user.setRole(userRequest.getRole());
        } else
            throw new InvalidUserRequestException("Wrong or non-existent role!");

        userRepository.save(user);

        return new UsersResponse("New user created successfully");
    }

    /**
     *
     * @param userId id of the user wanted
     * @return the found user
     */
    @Override
    public Users getById(Integer userId) {
        if(userRepository.findById(userId).isPresent()) {
            return userRepository.findById(userId).get();
        } else
            throw new RecordNotFoundException("User with the specified ID does not exist!");
    }

    /**
     *
     * @param usersRequest data input by the user, which will be used to retrieve data from DB
     * @return the user found by email and password
     */
    @Override
    public Users getByUserCredentials(UsersRequest usersRequest) {
        Users user = userRepository.findByEmail(usersRequest.getEmail());
        if (user != null) {
            if(encoder.matches(usersRequest.getPassword(), user.getPassword())){
                return user;
            } else
                throw new InvalidLoginDataException("Wrong login data provided");
        } else
            throw new InvalidLoginDataException("Wrong login data provided");
    }

    /**
     * @param userRequest ID of the user wanted for removal
     */
    @Override
    public void deleteUser(UsersRequest userRequest) {
        Users user = userRepository.findByEmail(userRequest.getEmail());
        if(user == null) {
            throw new InvalidLoginDataException("Wrong credentials provided!");
        }

        if(encoder.matches(userRequest.getPassword(),user.getPassword()) && userRequest.getEmail().equals(user.getEmail())) {
            userRepository.deleteById(user.getId());
        } else
            throw new InvalidLoginDataException("Wrong login info!");
    }

    /**
     *
     * @param email This is the email with which the search will be performed
     * @return Returns the user associated with this email
     */
    @Override
    public Users getByEmail(String email) {
        if(userRepository.findByEmail(email) != null) {
            return userRepository.findByEmail(email);
        } else
            throw new RecordNotFoundException("User with the specified ID does not exist!");
    }

    /**
     *
     * @return Returns a list containing all the administrators
     */
    @Override
    public List<Users> getAllAdminUsers() {
        if(!userRepository.findAllAdminUsers().isEmpty()) {
            return userRepository.findAllAdminUsers();
        } else
            throw new RecordNotFoundException("There are no administrators!");
    }

    @Override
    public void deleteAdminById(Integer id) {
        Users user = userRepository.getReferenceById(id);
        if(user == null) {
            throw new RecordNotFoundException("User with the specified ID does NOT exist!");
        }

        userRepository.deleteById(id);
    }

    /**
     *
     * @param id the ID of the user whose information will be updated
     * @param userRequest data input by the user, which will update the old data
     * @return the updated user information
     */
    @Override
    public Optional<Users> userUpdate(Integer id, UsersRequest userRequest) {
        Optional<Users> user = userRepository.findById(id);
        if(user.isPresent()) {

            if(userRequest.getFirstName() == null) {
                user.get().setFirstName(userRepository.getReferenceById(id).getFirstName());
            } else {
                user.get().setFirstName(userRequest.getFirstName());
            }

            if(userRequest.getLastName() == null) {
                user.get().setLastName(userRepository.getReferenceById(id).getLastName());
            } else {
                user.get().setLastName(userRequest.getLastName());
            }

            if(userRequest.getEmail() == null || userRequest.getEmail().isBlank()) {
                user.get().setEmail(userRepository.getReferenceById(id).getEmail());
            } else {
                user.get().setEmail(userRequest.getEmail());
            }

            if(userRequest.getPassword() == null) {
                user.get().setPassword(userRepository.getReferenceById(id).getPassword());
            } else {
                user.get().setPassword(encoder.encode(userRequest.getPassword()));
            }

            user.get().setActivated(true);

            user.get().setRole(userRepository.getReferenceById(id).getRole());

            userRepository.save(user.get());
            return user;
        } else
            throw new RecordNotFoundException("User with the specified ID not found");
    }
}
