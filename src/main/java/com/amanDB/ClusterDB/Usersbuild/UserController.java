package com.amanDB.ClusterDB.Usersbuild;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    public final UsersService usersService= new UsersService();

//    @RequestMapping(method = RequestMethod.POST , value="/initials/usersConfig")
//    public void writeUsers(@RequestBody List<User> users) throws Exception {
//        for (User user :
//                users) {
//            usersService.writeUser(user);
//        }
//    }

    @PostMapping
    public void registerNewUser(@RequestBody User user) throws Exception {
        usersService.writeUser(user);
    }


}
