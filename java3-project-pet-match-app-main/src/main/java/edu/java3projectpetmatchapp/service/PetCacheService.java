package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PetCacheService {

    private final PetRepository petRepo;

    @Cacheable("allPets")
    public List<Pet> getAllPets() {
        return petRepo.findAll();
    }
}