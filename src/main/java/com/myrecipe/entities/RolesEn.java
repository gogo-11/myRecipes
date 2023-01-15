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
    ADMIN("admin"),
    /**
     * Роля на обикновен потребител
     */
    USER("user");

    RolesEn (String roleName) {
        this.roleName = roleName;
    }

    private String roleName;
}
