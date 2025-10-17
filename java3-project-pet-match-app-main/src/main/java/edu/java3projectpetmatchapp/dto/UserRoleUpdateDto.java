package edu.java3projectpetmatchapp.dto;

import edu.java3projectpetmatchapp.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateDto {
    private Long id;
    private String firstName;
    private String lastName;

    @NotNull(message = "Role is required.")
    private Role role;
}
