package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.config.StripeConfig;
import edu.java3projectpetmatchapp.enums.ApplicationStatus;
import edu.java3projectpetmatchapp.enums.Availability;
import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.PetService;
import edu.java3projectpetmatchapp.service.StripeService;
import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/stripe")
public class StripeController {

    private final StripeService stripeService;
    private final ApplicationService applicationService;
    private final StripeConfig stripeConfig;
    private final PetService petService;

    @GetMapping("/checkout")
    public String showCheckoutPage(
            @RequestParam("appId") Long appId,
            @RequestParam("adopterEmail") String adopterEmail,
            @RequestParam("petName") String petName,
            Model model)
    {
        model.addAttribute("appId", appId);
        model.addAttribute("adopterEmail", adopterEmail);
        model.addAttribute("petName", petName);
        model.addAttribute("stripePublishableKey", stripeConfig.getPublishableKey());
        return "stripe/checkout";
    }

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestParam("adopterEmail") String adopterEmail,
            @RequestParam("petName") String petName,
            @RequestParam("applicationId") Long applicationId) {

        try {
            Map<String, String> response = stripeService.createCheckoutSession(adopterEmail, petName, applicationId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            System.err.println("Stripe API error creating session: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Payment service unavailable."));
        }
    }

    @GetMapping("/return")
    public String handleStripeReturn(
            @RequestParam("session_id") String sessionId,
            @RequestParam("app_id") Long applicationId,
            Model model)
    {
        try {
            Session session = stripeService.retrieveSession(sessionId);

            if ("complete".equals(session.getStatus())) {
                applicationService.updateApplicationStatus(applicationId, ApplicationStatus.valueOf("PAID_ADOPTION_FEE"));
                
                // Change pet availability to ADOPTED
                var application = applicationService.getAppById(applicationId);
                Long petId = application.getPet().getId();
                petService.updatePetAvailability(petId, Availability.ADOPTED);
            }

            model.addAttribute("status", session.getStatus());
            model.addAttribute("customerEmail", session.getCustomerDetails().getEmail());

            return "stripe/return";

        } catch (StripeException e) {
            e.printStackTrace();
            return "redirect:/profile?error=payment_failed";
        }
    }

    @GetMapping("/session-status")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSessionStatus(@RequestParam("session_id") String sessionId) {
        try {
            Session session = stripeService.retrieveSession(sessionId);
            return ResponseEntity.ok(Map.of(
                    "status", session.getStatus(),
                    "customer_email", session.getCustomerDetails().getEmail()
            ));
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve session"));
        }
    }
}