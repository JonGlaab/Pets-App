package edu.java3projectpetmatchapp.repository;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Optional<Pet> findPetById(long id);

    List<Pet> findAllByAvailabilityIsNot(Availability availability);
}