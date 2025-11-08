package dev.dsa.controller;

import dev.dsa.entity.RoleProfile;
import dev.dsa.repository.RoleRepository;
import dev.dsa.service.RoleProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin/profiles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class RoleProfileController {

    private final RoleProfileService roleProfileService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listProfiles(Model model) {
        log.info("Listing all role profiles");
        model.addAttribute("profiles", roleProfileService.getAllProfiles());
        return "admin/profiles/list";
    }

    @GetMapping("/new")
    public String newProfileForm(Model model) {
        log.info("Showing new profile form");
        model.addAttribute("profile", new RoleProfile());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/profiles/form";
    }

    @PostMapping
    public String createProfile(@Valid @ModelAttribute RoleProfile profile,
                               BindingResult result,
                               @RequestParam(value = "roleIds", required = false) Set<Long> roleIds,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("Creating profile: {}", profile.getName());

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/profiles/form";
        }

        try {
            roleProfileService.createProfile(profile, roleIds);
            redirectAttributes.addFlashAttribute("success", "Profile created successfully");
            return "redirect:/admin/profiles";
        } catch (IllegalArgumentException e) {
            log.error("Error creating profile", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/profiles/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editProfileForm(@PathVariable Long id, Model model) {
        log.info("Showing edit form for profile: {}", id);
        RoleProfile profile = roleProfileService.getProfileById(id)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        model.addAttribute("profile", profile);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/profiles/form";
    }

    @PostMapping("/{id}")
    public String updateProfile(@PathVariable Long id,
                               @Valid @ModelAttribute RoleProfile profile,
                               BindingResult result,
                               @RequestParam(value = "roleIds", required = false) Set<Long> roleIds,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("Updating profile: {}", id);

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin/profiles/form";
        }

        try {
            roleProfileService.updateProfile(id, profile, roleIds);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/admin/profiles";
        } catch (IllegalArgumentException e) {
            log.error("Error updating profile", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("profile", profile);
            return "admin/profiles/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProfile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting profile: {}", id);
        try {
            roleProfileService.deleteProfile(id);
            redirectAttributes.addFlashAttribute("success", "Profile deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting profile", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/profiles";
    }

    @GetMapping("/{id}")
    public String viewProfile(@PathVariable Long id, Model model) {
        log.info("Viewing profile: {}", id);
        RoleProfile profile = roleProfileService.getProfileById(id)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        model.addAttribute("profile", profile);
        return "admin/profiles/view";
    }
}
