package com.amanDB.ClusterDB.Usersbuild;

import com.amanDB.ClusterDB.FileManagment.DocumentReader;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Setter
@Getter
@NoArgsConstructor
public class UsersService {

    private User user;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public void writeUser(User user) throws Exception {
        DocumentWriter writer = DocumentWriter.getDocumentWriter();
        String path = "DatabaseUsers/" + user.getUsername() + ".json";
        String userJsonObj = new ObjectMapper().writeValueAsString(user);
        writer.write(path, userJsonObj);
    }

    public User buildUser(User user) {
        UserBuilder builder = new UserBuilder();
        return builder
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole()).build();
    }

    public static UserDetails getUserFromFiles(String username) throws Exception {
        DocumentReader documentReader = DocumentReader.getDocumentReader();
        String path = "DatabaseUsers/" + username + ".json";
        String userDocument;
        try {
            userDocument = documentReader.read(path);
        } catch (Exception ex) {
            throw new UsernameNotFoundException(username);
        }
        User user = new ObjectMapper().readValue(userDocument, User.class);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(user.getRole())
                .build();
    }

}
