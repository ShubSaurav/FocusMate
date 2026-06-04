package com.focusmate.model;

public class User {
    public Integer id;
    public String email;
    public String name;
    public String passwordHash;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
