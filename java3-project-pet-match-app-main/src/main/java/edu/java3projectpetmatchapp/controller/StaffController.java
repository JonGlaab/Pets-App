package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.dto.UpdatePetForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.ApplicationStatus;
import edu.java3projectpetmatchapp.enums.Availability;
import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.enums.Sociability;
import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {

    private final CustomUserDetailsService userService;
    private final PetService petService;
    private final ApplicationService appService;
    private final edu.java3projectpetmatchapp.service.S3StorageService s3Service;


    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/pets-list")
    public String showStaffDashboard(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "15") int size,
                                     @RequestParam(defaultValue = "id") String sort,
                                     @RequestParam(defaultValue = "asc") String dir,
                                     Model model) {

        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Pet> pets = petService.getAllPetsSortedAndPaginated(sort, direction, page, size);

        model.addAttribute("pets", pets.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pets.getTotalPages());
        model.addAttribute("totalItems", pets.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", direction.toString());
        model.addAttribute("reverseSortDir", direction == Sort.Direction.ASC ? "DESC" : "ASC");
        return "staff/pets-list";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/create-pet")
    public String showAddPetForm(Model model) {
        model.addAttribute("addPetForm", new AddPetForm());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        model.addAttribute("defaultPetPhotoUrl", s3Service.getDefaultPetPhotoUrl());
        return "staff/create-pet";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/create-pet")
    public String createPet(
            @ModelAttribute("addPetForm") @Valid AddPetForm form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            return "staff/create-pet";
        }
        try {
            petService.registerNewPet(form);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            model.addAttribute("error", "An error occurred while saving the pet.");
            return "staff/create-pet";
        }
        return "staff/pets-list";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/edit-pet")
    public String showUpdatePetPage(@RequestParam("id") Long id, Model model) {
        Pet pet = petService.getPetById(id);
        UpdatePetForm form = petService.convertPetToForm(pet);

        model.addAttribute("updatePetForm", form);
        model.addAttribute("currentPetPhotoUrl", pet.getPetPhotoUrl());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        model.addAttribute("availabilityOptions", Availability.values());
        return "staff/edit-pet";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/edit-pet")
    public String UpdatePet(
            @ModelAttribute("updatePetForm") @Valid UpdatePetForm form,
            BindingResult result,
            Model model) {

        Runnable reloadPhotoUrl = () -> {
            try {
                Pet pet = petService.getPetById(form.getId());
                model.addAttribute("currentPetPhotoUrl", pet.getPetPhotoUrl());
            } catch (NoSuchElementException ignored) {
            }
        };

        if (result.hasErrors()) {
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            model.addAttribute("availabilityOptions", Availability.values());
            reloadPhotoUrl.run();
            return "staff/edit-pet";
        }
        try {
            Pet petToUpdate = petService.getPetById(form.getId());
            petService.updatePet(form, petToUpdate);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            model.addAttribute("availabilityOptions", Availability.values());
            reloadPhotoUrl.run();
            model.addAttribute("error", "An error occurred while saving the pet.");
            return "staff/edit-pet";
        }
        return "redirect:/staff/pets-list";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/pet/{id}/delete")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            petService.deletePet(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pet deleted successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting pet and associated data: " + e.getMessage());
        }
        return "redirect:/staff/pets-list";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/applications-list")
    public String showStaffApplications(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "15") int size,
                                        @RequestParam(defaultValue = "id") String sort,
                                        @RequestParam(defaultValue = "asc") String dir,
                                        Model model) {

        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Application> applications = appService.getAllApplicationsSortedAndPaginated(sort, direction, page, size);


        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", applications.getTotalPages());
        model.addAttribute("totalItems", applications.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", direction.toString());
        model.addAttribute("reverseSortDir", direction == Sort.Direction.ASC ? "DESC" : "ASC");
        return "staff/applications-list";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/application/{id}")
    public String viewApplication(@PathVariable Long id, Model model) {
        try {
            Application application = appService.getAppById(id);
            model.addAttribute("app", application);
            model.addAttribute("applicationStatuses", ApplicationStatus.values());
            return "staff/application-review";
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return "redirect:/staff/application";
        }
    }

    @CacheEvict(value = "allApplications allPets", allEntries = true)
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/application/{id}/update-status")
    public String updateApplicationStatus(@PathVariable Long id, 
                                        @RequestParam("status") ApplicationStatus status,
                                        RedirectAttributes redirectAttributes) {
        try {
            appService.updateApplicationStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Application status updated successfully to " + status);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating application status: " + e.getMessage());
        }
        return "redirect:/staff/application/" + id;
    }

    @CacheEvict(value = "allApplications", allEntries = true)
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/application/{id}/delete")
    public String deleteApplication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appService.deleteApplication(id);
            redirectAttributes.addFlashAttribute("successMessage", "Application deleted successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting Application: " + e.getMessage());
        }
        return "redirect:/staff/applications-list";
    }
}