package org.local.websocketapp.Utils;



import lombok.RequiredArgsConstructor;

import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Models.UserPrincipal;
import org.local.websocketapp.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;





@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserC user = userRepository.findUserCByName(username).get();
        if (user == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }

        return new UserPrincipal(user);
    }
}
