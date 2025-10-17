package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.dto.UserProfileUpdateForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.Role;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final ApplicationRepository appRepo;
    private final S3StorageService s3Service;
    private final UserCacheService userCacheService;

    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    public void registerNewUser(RegistrationForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userRepo.findUserByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("That Email address is already in use");
        }
        User user = new User();
        user.setFirstName(form.getFirstName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(form.getEmail().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        user.setUserPhotoUrl(s3Service.getDefaultUserPhotoUrl());
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

    }
    public User getUserEntityByEmail(String email) {
        return userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public ProfileData getProfileData(String email) {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Application> applications = appRepo.findByUser(user);
        return new ProfileData(user, applications);
    }

    public void updateProfile(UserProfileUpdateForm form, String userEmail) throws Exception {
        User user = userRepo.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setBio(form.getBio());
        user.setPetPreference(form.getPetPreference());

        String currentPhotoUrl = user.getUserPhotoUrl();

        if (form.isDeletePhoto()) {

            if (!currentPhotoUrl.equals(s3Service.getDefaultUserPhotoUrl())) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }
            user.setUserPhotoUrl(s3Service.getDefaultUserPhotoUrl());

        } else if (form.getNewPhoto() != null && !form.getNewPhoto().isEmpty()) {

            if (currentPhotoUrl != null && !currentPhotoUrl.equals(s3Service.getDefaultUserPhotoUrl())) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }

            String newUrl = s3Service.uploadUserPhoto(form.getNewPhoto());
            user.setUserPhotoUrl(newUrl);
        }

        userRepo.save(user);
    }

    public Optional<User> getUserEntityById(Long id){
        return userRepo.findById(id);
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setRole(newRole);
        userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        appRepo.deleteByUser(user);
        userRepo.delete(user);
    }

    public Page<User> getUsersSortedAndPaginated(String sortField, Sort.Direction direction, int page, int size) {
        List<User> users = new ArrayList<>(userCacheService.getAllUsers());

        if (!isValidSortField(sortField)) {
            sortField = "id";
        }

        users.sort(getComparator(sortField, direction));

        int start = page * size;
        int end = Math.min(start + size, users.size());

        List<User> pageContent = (start > end) ? Collections.emptyList() : users.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size, Sort.by(direction, sortField)), users.size());
    }

    private boolean isValidSortField(String field) {
        return List.of("id", "email", "role").contains(field);
    }

    private Comparator<User> getComparator(String field, Sort.Direction direction) {
        Comparator<Comparable> baseComparator;

        switch (field) {
            case "email" -> baseComparator = (Comparator) String.CASE_INSENSITIVE_ORDER;
            case "role" -> baseComparator = Comparator.naturalOrder();
            default -> baseComparator = Comparator.naturalOrder();
        }

        if (direction == Sort.Direction.DESC) {
            baseComparator = baseComparator.reversed();
        }

        return switch (field) {
            case "email" -> Comparator.comparing(User::getEmail, Comparator.nullsLast(baseComparator));
            case "role" -> Comparator.comparing(User::getRole, Comparator.nullsLast(baseComparator));
            default -> Comparator.comparing(User::getId, Comparator.nullsLast(baseComparator));
        };
    }

}