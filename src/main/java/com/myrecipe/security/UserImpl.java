package com.myrecipe.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Objects;

public class UserImpl extends User {
    private String firstName;

    public UserImpl(String username, String password, String firstName, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserImpl user = (UserImpl) o;

        return Objects.equals(firstName, user.firstName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getFirstName() + " " + super.toString();
    }
}
