package com.myrecipe.service;

import com.myrecipe.entities.RolesEn;
import com.myrecipe.entities.Users;
import com.myrecipe.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AdminInitializer {
    private UsersRepository repository;
    private PasswordEncoder encoder;

    public AdminInitializer(UsersRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @PostConstruct
    public void init() {
        if (!adminExists()) {
            createAdmin();
        }
    }

    private void createAdmin() {
        Users admin = new Users();
        admin.setActivated(true);
        admin.setRole(RolesEn.ADMIN);
        admin.setFirstName("Администратор");
        admin.setLastName("Първи");
        admin.setEmail("admin@my-recipes.com");
        admin.setPassword(encoder.encode("adminpass"));

        repository.save(admin);
    }

    private boolean adminExists() {
        return repository.findAllAdminUsers() != null;
    }
}
