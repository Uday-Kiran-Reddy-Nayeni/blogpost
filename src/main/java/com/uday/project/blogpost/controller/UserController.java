package com.uday.project.blogpost.controller;


import com.uday.project.blogpost.model.User;
import com.uday.project.blogpost.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    UserService userService;
    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    @RequestMapping("/login")
    public String userLogin() {
        return "userLogin";
    }

    @RequestMapping("/register")
    public String newUserRegister(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "register";
    }

    @RequestMapping("/saveuser")
    public String saveUser(@ModelAttribute("user") User user, Model model) {
        System.out.println(user);
        user.setPassword(encoder().encode(user.getPassword()));
        user.setRole("AUTHOR");
        userService.saveUser(user);
        return "userLogin";
    }
}
