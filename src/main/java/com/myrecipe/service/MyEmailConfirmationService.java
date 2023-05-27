package com.myrecipe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.entities.EmailConfirmationToken;
import com.myrecipe.entities.requests.EmailConfirmationTokenRequest;
import com.myrecipe.entities.responses.EmailConfirmationResponse;
import com.myrecipe.repository.EmailConfirmationTokenRepository;
import com.myrecipe.repository.UsersRepository;

@Service
public class MyEmailConfirmationService implements EmailConfirmationService {
    @Autowired
    private EmailConfirmationTokenRepository emailRepository;

    @Autowired
    private UsersRepository usersRepository;

    /**
     * @param token
     * @return
     */
    @Override
    public EmailConfirmationToken getByToken(String token) {
        if(emailRepository.findByToken(token) != null) {
            return emailRepository.findByToken(token);
        } else
            throw new RecordNotFoundException("Невалиден токен!");
    }

    /**
     * @param userId Integer value. ID of the user
     * @return EmailConfirmationToken object if found
     */
    @Override
    public EmailConfirmationToken getByUserId(Integer userId) {
        if(emailRepository.findByUser(userId) != null) {
            return emailRepository.findByUser(userId);
        } else
            throw new RecordNotFoundException("The user has not been registered before!");
    }

    /**
     * @param request gets a request object of type EmailConfirmationTokenRequest
     * @return Response for successfully created and stored token
     */
    @Override
    public EmailConfirmationResponse createNewEmailConfirmationToken(EmailConfirmationTokenRequest request) {
        EmailConfirmationToken emailToken = new EmailConfirmationToken();

        if(!request.getToken().isBlank()) {
            emailToken.setToken(request.getToken());
        } else
            throw new InvalidUserRequestException("No token generated!");

        if(request.getUserId() != null) {
            emailToken.setUser(usersRepository.getReferenceById(request.getUserId()));
        } else
            throw new InvalidUserRequestException("No user ID found!");

        emailRepository.save(emailToken);

        return new EmailConfirmationResponse("Токенът за потвърждение на имейла е успешно записан");
    }

    /**
     * @param id Integer value by which a database search will be performed
     * @return EmailConfirmationToken object if found in the database
     * @throws RecordNotFoundException if a token is not found
     */
    @Override
    public EmailConfirmationToken getByTokenId(Integer id) {
        if(emailRepository.findById(id).isPresent()) {
            return emailRepository.findById(id).get();
        } else
            throw new RecordNotFoundException("User with the specified ID does not exist!");
    }

    /**
     * @param id Integer value of the token wanted for deletion
     */
    @Override
    public void deleteToken(Integer id) {
        if(emailRepository.findById(id).isPresent()) {
            emailRepository.deleteById(emailRepository.findById(id).get().getId());
        } else
            throw new RecordNotFoundException("No token with the specified id found!");
    }
}
