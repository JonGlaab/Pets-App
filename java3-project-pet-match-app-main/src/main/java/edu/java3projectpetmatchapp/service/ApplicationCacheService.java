package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ApplicationCacheService {

    private final ApplicationRepository appRepo;

    @Cacheable("allApplications")
    public List<Application> getAllApplications() {
        return appRepo.findAll();
    }

    @CacheEvict(value = "allApplications", allEntries = true)
    public void evictAllApplications() {
    }
}