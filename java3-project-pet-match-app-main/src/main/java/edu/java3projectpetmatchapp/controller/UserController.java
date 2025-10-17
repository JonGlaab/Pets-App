package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.PetApplicationForm;
import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.UserProfileUpdateForm;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.*;
import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final CustomUserDetailsService userService;
    private final PetService petService;
    private final ApplicationService appService;

    @GetMapping("/user/profile")
    public String showOwnProfile(Model model, Principal principal) {
        String targetEmail = principal.getName();
        ProfileData profileData = userService.getProfileData(targetEmail);
        model.addAttribute("user", profileData.getUser());
        model.addAttribute("applications", profileData.getApplications());
        return "user/profile";
    }

    @CacheEvict(value = "allUsers", allEntries = true)
    @PostMapping("/profile/edit")
    public String handleProfileUpdate(
            @ModelAttribute("profileUpdateForm") @Valid UserProfileUpdateForm form,
            BindingResult result,
            Principal principal,
            Model model) {

        Runnable reloadModel = () -> {
            User userEntity = userService.getUserEntityByEmail(principal.getName());
            model.addAttribute("user", userEntity);
            model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());
        };

        if (result.hasErrors()) {

            User userEntity = userService.getUserEntityByEmail(principal.getName());

            model.addAttribute("user", userEntity);
            model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());

            return "user/edit-profile";
        }

        try {
            userService.updateProfile(form, principal.getName());
        } catch (Exception e) {

            // Log the exception for debugging
            System.err.println("Error updating profile: " + e.getMessage());
            result.reject("globalError", "Could not save profile due to a system error. Please try again.");
            reloadModel.run();

            return "user/edit-profile";
        }

        return "redirect:/user/profile";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/profile/edit")
    public String showProfileEditForm(Model model, Principal principal) {

        User userEntity = userService.getUserEntityByEmail(principal.getName());
        UserProfileUpdateForm form = new UserProfileUpdateForm();
        form.setFirstName(userEntity.getFirstName());
        form.setLastName(userEntity.getLastName());
        form.setBio(userEntity.getBio());
        form.setPetPreference(userEntity.getPetPreference());

        model.addAttribute("user", userEntity);

        model.addAttribute("profileUpdateForm", form);

        model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());

        return "user/edit-profile";
    }

    @GetMapping("/pet/{id}/apply")
    public String showPetApplicationForm(@PathVariable Long id,
                                         Model model,
                                         Principal principal) {
        Pet pet = petService.getPetById(id);
        User user = userService.getUserEntityByEmail(principal.getName());

        PetApplicationForm form = new PetApplicationForm();
        form.setPet(pet);
        form.setUser(user);

        model.addAttribute("petApplicationForm", form);
        model.addAttribute("homeType", HomeType.values());
        model.addAttribute("householdSituation", HouseholdSituation.values());
        model.addAttribute("otherPets", OtherPets.values());

        return "user/pet-apply";
    }

    @CacheEvict(value = "allApplications", allEntries = true)
    @PostMapping("/pet/{id}/apply")
    public String applyForPet(@PathVariable Long id,
                              @ModelAttribute("petApplicationForm")
                              @Valid PetApplicationForm form,
                              BindingResult result,
                              Model model) {

        if (result.hasErrors()) {
            model.addAttribute("homeType", HomeType.values());
            model.addAttribute("householdSituation", HouseholdSituation.values());
            model.addAttribute("otherPets", OtherPets.values());
            return "user/pet-apply";
        }
        try {
            appService.registerNewApplication(form);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("homeType", HomeType.values());
            model.addAttribute("householdSituation", HouseholdSituation.values());
            model.addAttribute("otherPets", OtherPets.values());
            model.addAttribute("error", "An error occurred while saving the application.");
            return "user/pet-apply";
        }
        return "redirect:/pet/" + form.getPet().getId();
    }
}