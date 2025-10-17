package edu.java3projectpetmatchapp.entity;

import edu.java3projectpetmatchapp.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString(exclude = {"user", "pet"})
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "applications_table")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="application_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(name = "yard_access")
    private Boolean yardAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "other_pets", length = 20)
    private OtherPets otherPets;

    @Enumerated(EnumType.STRING)
    @Column(name = "household_situation", length = 10)
    private HouseholdSituation householdSituation;

    @Enumerated(EnumType.STRING)
    @Column(name = "home_type", length = 30)
    private HomeType hometype;

    @NotBlank
    @Size(max = 1000,
            message="Must be less than 1000 characters")
    @Column(name = "additional_info", length = 1000)
    private String additionalInfo;

    @Column(name = "date_app_received", nullable = false)
    private LocalDate dateAppReceived;

    @PrePersist
    public void prePersist() {
        this.dateAppReceived = LocalDate.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 15)
    private ApplicationStatus applicationStatus = ApplicationStatus.RECEIVED;
}