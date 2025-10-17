package edu.java3projectpetmatchapp.entity;

import edu.java3projectpetmatchapp.enums.PetPreference;
import edu.java3projectpetmatchapp.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users_table")
@Entity
public class User {

    @PrePersist
    public void assignDefaults() {
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.petPreference == null) {
            this.petPreference = PetPreference.NONE;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 1 and 40 characters")
    @Column(name = "first_name", length = 40, nullable = false)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 1 and 40 characters")
    @Column(name = "last_name", length = 40, nullable = false)
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "email", length = 360, nullable = false, unique = true)
    private String email;

    @Column(length = 80, name = "password", nullable = false)
    private String password;

    @Transient
    private String confirmPassword;

    @Column(name = "user_bio", length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_preference", length = 20)
    private PetPreference petPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", length = 10, nullable = false)
    private Role role;

    @Column(name = "user_photo_url")
    private String userPhotoUrl;
}