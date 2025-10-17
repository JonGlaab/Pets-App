package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserCacheService {

    private final UserRepository userRepo;

    @Cacheable("allUsers")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}