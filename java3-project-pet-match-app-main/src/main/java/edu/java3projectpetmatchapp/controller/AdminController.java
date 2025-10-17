package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.UserRoleUpdateDto;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

@Controller
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CustomUserDetailsService userService;

    @GetMapping("/users-list")
    public String showUserList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "15") int size,
                               @RequestParam(defaultValue = "id") String sort,
                               @RequestParam(defaultValue = "asc") String dir,
                               Model model) {


        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<User> users = userService.getUsersSortedAndPaginated(sort, direction, page, size);

        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", direction.toString());
        model.addAttribute("reverseSortDir", direction == Sort.Direction.ASC ? "DESC" : "ASC");
        return "admin/users-list";
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showOtherProfile(@PathVariable Long id, Model model) {
        User targetUser = userService.getUserEntityById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        ProfileData profileData = userService.getProfileData(targetUser.getEmail());
        model.addAttribute("user", profileData.getUser());
        model.addAttribute("applications", profileData.getApplications());
        return "user/profile";
    }

    @GetMapping("/user/{id}/edit-role")
    public String showEditRoleForm(@PathVariable Long id, Model model){

        User userEntity = userService.getUserEntityById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));

        UserRoleUpdateDto form = new UserRoleUpdateDto();
        form.setId(userEntity.getId());
        form.setFirstName(userEntity.getFirstName());
        form.setLastName(userEntity.getLastName());
        form.setRole(userEntity.getRole());

        model.addAttribute("user", userEntity);
        model.addAttribute("userRoleUpdateDto", form);
        return "admin/update-user-role";
    }

    @CacheEvict(value = "allUsers", allEntries = true)
    @PostMapping("/user/{id}/edit-role")
    public String handleRoleUpdate(
            @PathVariable Long id,
            @ModelAttribute("userRoleUpdateDto") @Valid UserRoleUpdateDto form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRoleUpdateDto", result);
            redirectAttributes.addFlashAttribute("userRoleUpdateDto", form);

            return "redirect:/admin/user/{id}/edit";
        }

        try {
            userService.updateUserRole(id, form.getRole());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role for " + form.getFirstName() + " updated successfully!");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating role: " + e.getMessage());
        }
        return "redirect:/admin/users-list";
    }

    @CacheEvict(value = "allUserss", allEntries = true)
    @PostMapping("/user/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user and associated data: " + e.getMessage());
        }
        return "redirect:/admin/users-list";
    }
}