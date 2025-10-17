package edu.java3projectpetmatchapp.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import edu.java3projectpetmatchapp.config.StripeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private final StripeConfig stripeConfig;

    @Value("${app.base-url}")
    private String baseUrl;

    public StripeService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    public Map<String, String> createCheckoutSession(String adopterEmail, String petName, Long applicationId)
            throws StripeException {

        Stripe.apiKey = stripeConfig.getSecretKey();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(adopterEmail)
                .setSuccessUrl(baseUrl + "/stripe/return?session_id={CHECKOUT_SESSION_ID}&app_id=" + applicationId)
                .setCancelUrl(baseUrl + "/stripe/checkout?appId=" + applicationId + "&adopterEmail=" + adopterEmail + "&petName=" + petName)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("cad")
                                                .setUnitAmount(10000L)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Adoption Fee for " + petName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("id", session.getId());
        return response;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        Stripe.apiKey = stripeConfig.getSecretKey();
        return Session.retrieve(sessionId);
    }
}