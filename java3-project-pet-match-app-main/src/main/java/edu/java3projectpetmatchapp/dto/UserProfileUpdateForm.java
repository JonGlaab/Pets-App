package edu.java3projectpetmatchapp.dto;

import edu.java3projectpetmatchapp.enums.PetPreference;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserProfileUpdateForm {

    @Size(min = 2, max = 40)
    private String firstName;

    @Size(min = 2, max = 40)
    private String lastName;

    @Size(max = 500)
    private String bio;

    private PetPreference petPreference;

    // For uploading a new photo
    private MultipartFile newPhoto;

    // Hidden field to flag if the user explicitly wants to delete the current photo
    private boolean deletePhoto;
}