package com.graphaware.pizzeria.security;

import com.graphaware.pizzeria.model.PizzeriaUser;
import com.graphaware.pizzeria.repository.PizzeriaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PizzeriaUserDetailsService implements UserDetailsService {

    private final PizzeriaUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        PizzeriaUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new PizzeriaUserPrincipal(user);
    }
}
