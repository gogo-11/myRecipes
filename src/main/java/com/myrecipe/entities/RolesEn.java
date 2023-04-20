package com.myrecipe.entities;

import lombok.Getter;

/**
 * Възможните роли на потербителите на сайта
 *
 * {@link #ADMIN}
 * {@link #USER}

 */
@Getter
public enum RolesEn {
    /**
     * Роля на адмонистратор
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * Роля на обикновен потребител
     */
    USER("ROLE_USER");

    RolesEn (String roleName) {
        this.roleName = roleName;
    }

    private String roleName;
}
