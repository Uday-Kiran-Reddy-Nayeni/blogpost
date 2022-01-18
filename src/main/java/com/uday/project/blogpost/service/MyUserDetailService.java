package com.uday.project.blogpost.service;


import com.uday.project.blogpost.model.User;
import com.uday.project.blogpost.repository.UserRepository;
import com.uday.project.blogpost.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }
        MyUserDetails myUserDetails = new MyUserDetails(user);
        return myUserDetails;
    }
}
