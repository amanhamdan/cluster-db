package com.amanDB.ClusterDB.Usersbuild;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private String role;
    private String userID;

     User(String userID, String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.userID = userID;
    }
}
