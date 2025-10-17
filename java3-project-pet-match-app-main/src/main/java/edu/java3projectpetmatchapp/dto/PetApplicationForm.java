package edu.java3projectpetmatchapp.dto;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.ApplicationStatus;
import edu.java3projectpetmatchapp.enums.HomeType;
import edu.java3projectpetmatchapp.enums.HouseholdSituation;
import edu.java3projectpetmatchapp.enums.OtherPets;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PetApplicationForm {

    private MultipartFile newPhoto;

    private User user;

    private Pet pet;

    private Boolean yardAccess;

    private OtherPets otherPets;

    private HouseholdSituation householdSituation;

    private HomeType homeType;

    @NotBlank (message = "You must say something about yourself")
    @Size(max = 1000,
            message="Must be less than 1000 characters")
    @Column(name = "additional_info", length = 1000)
    private String additionalInfo;

    private ApplicationStatus applicationStatus = ApplicationStatus.RECEIVED;
}
