package com.company;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class Mailer {

    public static void sendMail(String subject, String message) {

        try{
            Email email = EmailBuilder.startingBlank()
                    .from("NRGBotStatus", "fromemail")
                    .toMultiple(Main.config.getList(String.class,"users.email"))
                    .withSubject(subject)
                    .withPlainText(message)
                    .buildEmail();

            MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 25, "youremail", "yourpassword")
                    .buildMailer()
                    .sendMail(email);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
