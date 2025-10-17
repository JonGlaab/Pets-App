package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class PublicController {

    private final CustomUserDetailsService userService;
    private final PetService petService;

    @GetMapping({"/", "/index", "/home"})
    public String viewIndex(Model model) {
        model.addAttribute("pets", petService.getAllAvailablePets());
        return "public/index";
    }

    @GetMapping("/login")
    public String viewLogin() {
        return "public/login";
    }

    //I have this here so there can be a popup or warning before logging out. Maybe it doesn't need to be its own page though
    @GetMapping("/logout")
    public String viewLogout() {
        return "logout";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "public/register";
    }

    @CacheEvict(value = "allUsers", allEntries = true)
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("registrationForm") @Valid RegistrationForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "public/register";
        }

        try {
            userService.registerNewUser(form);
        } catch (IllegalArgumentException e) {
            result.rejectValue("confirmPassword", "error.confirmPassword", e.getMessage());
            return "public/register";
        }

        return "redirect:/login";
    }

    //ajax test
    @GetMapping("/pet/filter")
    public String filterPets(
            @RequestParam(required = false) String petType,
            @RequestParam(required = false) String petAge,
            @RequestParam(required = false) String datePetSheltered,
            Model model
    ) {
        List<Pet> pets = petService.getFilteredPets(petType, petAge, datePetSheltered);
        model.addAttribute("pets", pets);
        return "layout/fragments.html :: petcards";
    }

    @GetMapping("/pet/{id}")
    public String viewPet(@PathVariable Long id, Model model) {
        Pet pet = petService.getPetById(id);
        model.addAttribute("pet", pet);
        return "public/pet-detail";
    }
}