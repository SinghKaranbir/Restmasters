package com.spartan.karanbir.attendance.util;

/**
 * Created by karanbir on 5/10/16.
 */
public class User {
    private String userType;
    private String firstName;
    private String lastName;
    private String email;

    public User(String userType, String firstName, String lastName, String email) {
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }
}
