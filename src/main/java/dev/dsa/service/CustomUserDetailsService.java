package dev.dsa.service;

import dev.dsa.entity.Permission;
import dev.dsa.entity.User;
import dev.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsernameWithRoles(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        log.debug("User {} loaded with {} authorities", username, authorities.size());

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .disabled(!user.getEnabled())
            .accountExpired(!user.getAccountNonExpired())
            .accountLocked(!user.getAccountNonLocked())
            .credentialsExpired(!user.getCredentialsNonExpired())
            .authorities(authorities)
            .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            log.debug("Added role: ROLE_{}", role.getName());
        });

        // Add permission-based authorities (including inherited permissions)
        Set<Permission> allPermissions = user.getAllPermissions();
        allPermissions.forEach(permission -> {
            if (permission.getActive()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
                log.debug("Added permission: {}", permission.getName());
            }
        });

        return authorities;
    }
}
