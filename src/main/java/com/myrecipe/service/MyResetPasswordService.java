package com.myrecipe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myrecipe.entities.PasswordResetToken;
import com.myrecipe.entities.requests.PasswordResetTokenRequest;
import com.myrecipe.entities.responses.PasswordResetTokenResponse;
import com.myrecipe.exceptions.InvalidUserRequestException;
import com.myrecipe.exceptions.RecordNotFoundException;
import com.myrecipe.repository.PasswordResetTokenRepository;
import com.myrecipe.repository.UsersRepository;

@Service
public class MyResetPasswordService implements ResetPasswordService{

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public PasswordResetToken getByToken(String token) {
        if(tokenRepository.findByToken(token) != null) {
            return tokenRepository.findByToken(token);
        } else
            throw new RecordNotFoundException("Invalid token!");
    }

    @Override
    public PasswordResetToken getByUserId(Integer id) {
        if(tokenRepository.findByUser(id) != null) {
            return tokenRepository.findByUser(id);
        } else
            throw new RecordNotFoundException("The user has either not requested a reset password token or the token has expired!");
    }

    /**
     * @param request gets a request object of type PasswordResetTokenRequest
     * @return Response for successfully created and stored token
     */
    @Override
    public PasswordResetTokenResponse createNewResetPasswordToken(PasswordResetTokenRequest request) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        if(!request.getToken().isBlank()) {
            passwordResetToken.setToken(request.getToken());
        } else
            throw new InvalidUserRequestException("No token generated!");

        if(request.getUserId() != null) {
            passwordResetToken.setUser(usersRepository.getReferenceById(request.getUserId()));
        } else
            throw new InvalidUserRequestException("No user ID found!");

        if(request.getExpirationDateTime() != null) {
            passwordResetToken.setExpirationDateTime(request.getExpirationDateTime());
        } else
            throw new InvalidUserRequestException("No expiration date set!");

        tokenRepository.save(passwordResetToken);

        return new PasswordResetTokenResponse("Password reset token created and stored successfully");
    }

    /**
     * @param id Integer value by which a database search will be performed
     * @return PasswordResetToken object if found in the database
     * @throws RecordNotFoundException if a token is not found
     */
    @Override
    public PasswordResetToken getByTokenId(Integer id) {
        if(tokenRepository.findById(id).isPresent()) {
            return tokenRepository.findById(id).get();
        } else
            throw new RecordNotFoundException("User with the specified ID does not exist!");
    }

    public MyResetPasswordService(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.tokenRepository = passwordResetTokenRepository;
    }

    /**
     * @param id
     */
    @Override
    public void deleteToken(Integer id) {
        if(tokenRepository.findById(id).isPresent()) {
            tokenRepository.deleteById(tokenRepository.findById(id).get().getId());
        } else
            throw new RecordNotFoundException("No token with the specified id found!");

    }
}
