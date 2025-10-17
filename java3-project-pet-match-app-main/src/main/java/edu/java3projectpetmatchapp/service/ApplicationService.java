package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.PetApplicationForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.ApplicationStatus;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static edu.java3projectpetmatchapp.enums.Availability.*;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final PetService petService;
    private final EmailService emailService;
    private final ApplicationRepository appRepo;
    private final ApplicationCacheService appCacheService;

    public Application getAppById(Long id) {
        return appRepo.findAppById(id)
                .orElseThrow(() -> new NoSuchElementException("No application found with ID: " + id));
    }

    public List<Application> getAllApplications() { return appRepo.findAll(); }

    public void registerNewApplication(PetApplicationForm form) {
        Application app = new Application();
        app.setUser(form.getUser());
        app.setPet(form.getPet());
        app.setYardAccess(form.getYardAccess());
        app.setOtherPets(form.getOtherPets());
        app.setHouseholdSituation(form.getHouseholdSituation());
        app.setHometype(form.getHomeType());
        app.setAdditionalInfo(form.getAdditionalInfo());
        app.setDateAppReceived(LocalDate.now());

        appRepo.save(app);
    }

    public Page<Application> getAllApplicationsSortedAndPaginated(String sortField, Sort.Direction direction, int page, int size) {
        List<Application> applications = new ArrayList<>(appCacheService.getAllApplications());

        if (!isValidSortField(sortField)) {
            sortField = "id";
        }

        applications.sort(getComparator(sortField, direction));

        int start = page * size;
        int end = Math.min(start + size, applications.size());

        List<Application> pageContent = (start > end) ? Collections.emptyList() : applications.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size, Sort.by(direction, sortField)), applications.size());
    }

    private boolean isValidSortField(String field) {
        return List.of("email", "petName", "dateAppReceived", "status").contains(field);
    }

    private Comparator<Application> getComparator(String field, Sort.Direction direction) {
        Comparator<Application> comparator = switch (field) {
            case "petName" -> Comparator.comparing(
                    app -> app.getPet() != null ? app.getPet().getPetName() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            );
            case "email" -> Comparator.comparing(
                    app -> app.getUser() != null ? app.getUser().getEmail() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            );
            case "status" -> Comparator.comparing(Application::getApplicationStatus);
            default -> Comparator.comparing(Application::getDateAppReceived);
        };

        return direction == Sort.Direction.DESC ? comparator.reversed() : comparator;
    }


    public void updateApplicationStatus(Long appId, ApplicationStatus newStatus) {
        Application app = getAppById(appId);
        Pet pet = app.getPet();
        app.setApplicationStatus(newStatus);
        if (String.valueOf(newStatus).equals("APPROVED")) {
            petService.updatePetAvailability(pet.getId(), RESERVED);
        }
        if (String.valueOf(newStatus).equals("PAID_ADOPTION_FEE")) {
            petService.updatePetAvailability(pet.getId(), ADOPTED);
        }
        if (String.valueOf(newStatus).equals("IN_REVIEW") || String.valueOf(newStatus).equals("REJECTED")) {
            petService.updatePetAvailability(pet.getId(), AVAILABLE);
        }

        //email logic hidden until properly able to email all endpoints. otherwise it crashes
        //emailService.sendStatusChangeEmail(app.getUser(), app, String.valueOf(app.getApplicationStatus()));
        appRepo.save(app);
        appCacheService.evictAllApplications();
    }

    public void deleteApplication(Long id) {
        Application app = appRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Application not found."));

        appRepo.delete(app);
    }
}