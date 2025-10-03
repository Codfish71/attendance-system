package com.geeksmetrics.attendance_mgmt.security.services;


import com.geeksmetrics.attendance_mgmt.entity.User;
import com.geeksmetrics.attendance_mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return UserDetailsServiceImpl.build(user);
    }
}