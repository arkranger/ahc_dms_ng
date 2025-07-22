package ahc.dms.security;

import ahc.dms.dao.dms.repositories.UserRepository;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //User user = userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User", "Email "+username, 0));
        return userRepository
                .findByUsername(username)
                .map(user -> {
                    if (Boolean.FALSE.equals(user.getStatus())) {
                        throw new ApiException("User is disabled");
                    }
                    return user;
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
