package com.amanDB.ClusterDB.security;


import com.amanDB.ClusterDB.Usersbuild.UsersService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsersService usersService = new UsersService();
        try {
            return UsersService.getUserFromFiles(username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
