package edu.java3projectpetmatchapp.dto;

import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProfileData {
    private User user;
    private List<Application> applications;
}

