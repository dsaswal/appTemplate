package dev.dsa.service;

import dev.dsa.entity.User;
import dev.dsa.entity.UserProfile;
import dev.dsa.exception.ResourceNotFoundException;
import dev.dsa.repository.UserProfileRepository;
import dev.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserProfile createDefaultProfile(User user) {
        log.info("Creating default profile for user: {}", user.getUsername());

        UserProfile profile = UserProfile.builder()
            .user(user)
            .pageSize(11)
            .timezone("UTC")
            .language("en")
            .dateFormat("yyyy-MM-dd")
            .timeFormat("HH:mm:ss")
            .theme("light")
            .build();

        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> getProfileByUsername(String username) {
        return userProfileRepository.findByUser_Username(username);
    }

    @Transactional(readOnly = true)
    public UserProfile getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        String username = authentication.getName();
        return getProfileByUsername(username)
            .orElseGet(() -> {
                // Create default profile if it doesn't exist
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
                return createDefaultProfile(user);
            });
    }

    @Transactional(readOnly = true)
    public int getCurrentUserPageSize() {
        try {
            UserProfile profile = getCurrentUserProfile();
            return profile.getPageSize() != null ? profile.getPageSize() : 11;
        } catch (Exception e) {
            log.warn("Could not retrieve user profile, using default page size", e);
            return 11;
        }
    }

    @Transactional
    public UserProfile updateProfile(Long userId, UserProfile profileDetails) {
        log.info("Updating profile for user ID: {}", userId);

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile not found for user: " + userId));

        if (profileDetails.getPageSize() != null && profileDetails.getPageSize() > 0 && profileDetails.getPageSize() <= 100) {
            profile.setPageSize(profileDetails.getPageSize());
        }
        if (profileDetails.getTimezone() != null) {
            profile.setTimezone(profileDetails.getTimezone());
        }
        if (profileDetails.getLanguage() != null) {
            profile.setLanguage(profileDetails.getLanguage());
        }
        if (profileDetails.getDateFormat() != null) {
            profile.setDateFormat(profileDetails.getDateFormat());
        }
        if (profileDetails.getTimeFormat() != null) {
            profile.setTimeFormat(profileDetails.getTimeFormat());
        }
        if (profileDetails.getTheme() != null) {
            profile.setTheme(profileDetails.getTheme());
        }

        return userProfileRepository.save(profile);
    }
}
