package hattabi.youness.ebanking_backend.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return switch (username) {
            case "admin" -> User.withUsername("admin")
                    .password(passwordEncoder.encode("1234"))
                    .roles("ADMIN", "USER")
                    .build();
            case "user" -> User.withUsername("user")
                    .password(passwordEncoder.encode("1234"))
                    .roles("USER")
                    .build();
            default -> throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}
