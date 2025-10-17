package edu.java3projectpetmatchapp.entity;

import edu.java3projectpetmatchapp.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pets_table")
public class Pet {

    @PrePersist
    public void assignDefaults() {
        if (this.sociability == null) {
            this.sociability = Collections.singleton(Sociability.UNKNOWN);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="pet_id")
    private Long id;

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 2 and 40 characters")
    @Column(name = "pet_name", length = 40, nullable = false)
    private String petName;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", length = 50, nullable = false)
    private PetType petType;

    @Column(name = "pet_breed", length = 100)
    private String petBreed;

    @ElementCollection(targetClass = Sociability.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "pet_sociability", joinColumns = @JoinColumn(name = "pet_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "sociability")
    private Set<Sociability> sociability = new HashSet<>();


    @Column(name = "special_needs", length = 500)
    private String specialNeeds;

    @Column(name = "health_issues", length = 500)
    private String healthIssues;

    @Column(name = "about", length = 1000)
    private String about;

    @Column(name = "age", length = 3)
    private int age;

    @Column(name = "date_pet_sheltered", nullable = false)
    private LocalDate datePetSheltered;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", length = 50, nullable = false)
    private Availability availability = Availability.AVAILABLE;

    @Column(name = "pet_photo_url")
    private String petPhotoUrl;
}