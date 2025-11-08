package dev.dsa.controller;

import dev.dsa.entity.UserProfile;
import dev.dsa.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String viewProfile(Model model) {
        log.info("Viewing user profile");
        UserProfile profile = userProfileService.getCurrentUserProfile();
        model.addAttribute("profile", profile);
        return "profile/view";
    }

    @GetMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(Model model) {
        log.info("Editing user profile");
        UserProfile profile = userProfileService.getCurrentUserProfile();
        model.addAttribute("profile", profile);
        return "profile/edit";
    }

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfile profileDetails,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("Updating user profile");

        if (result.hasErrors()) {
            return "profile/edit";
        }

        try {
            UserProfile currentProfile = userProfileService.getCurrentUserProfile();
            userProfileService.updateProfile(currentProfile.getUser().getId(), profileDetails);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Error updating profile", e);
            model.addAttribute("error", "Error updating profile: " + e.getMessage());
            return "profile/edit";
        }
    }
}
