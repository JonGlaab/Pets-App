package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {

    @Autowired
    private final JavaMailSender mailSender;

    public void sendStatusChangeEmail(User user, Application app, String newStatus) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your Application Status Has Changed");
        if (newStatus.equals("APPROVED")) {
            message.setText("Dear " + user.getFirstName() + ",\n\nWe are pleased to inform you that your application for "+ app.getPet().getPetName() +" has been approved! " +
                    "login and view your applications under your profile to find a payment link." +
                    " To finalize the process just follow the link and complete the payment.");
        }
        if (newStatus.equals("REJECTED")) {
            message.setText("Dear " + user.getFirstName() + ",\n\nWe are sorry to inform you that your application for "+ app.getPet().getPetName() +
                    " has been rejected at this time.\n\nRegards,\nYour Pets Team");
        }
        if (newStatus.equals("IN_REVIEW")) {
        message.setText("Dear " + user.getFirstName() + ",\n\nYour application for "+ app.getPet().getPetName() +
                " is under review. We will contact you again once we have a decision.\n\nRegards,\nYour Team");
        }

        mailSender.send(message);
    }
}