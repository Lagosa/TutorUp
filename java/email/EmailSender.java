package itreact.tutorup.server.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import itreact.tutorup.server.config.Configuration;
import itreact.tutorup.server.config.ConfigurationFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {
    private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);
    private static EmailSender ourInstance = new EmailSender();

    private final Configuration configuration;

    public static EmailSender getInstance() {
        return ourInstance;
    }

    private EmailSender() {
        configuration = ConfigurationFactory.getInstance();
    }

    public void sendEmail(String to, String subject, String content) {
        LOG.info("SENDING E-MAIL...\n\tFROM: {}\n\tTO: {}\n\tSUBJECT: {}\n\tCONTENT\n{}", configuration.getEmailFrom(),
                to, subject, content);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                LOG.debug("Preparing PasswordAuthentication with userName={} passwd={}", 
                        configuration.getGmailUsername(), configuration.getGmailPassword());
                return new PasswordAuthentication(configuration.getGmailUsername(), configuration.getGmailPassword());
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(configuration.getEmailFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (MessagingException e) {
            LOG.error("Failed to send e-mail", e);
        }
    }
}
