package org.apache.sling.mediacenter.notifications.components;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;


import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;


/**
 * @author Cosmin Stanciu
 */
@Component(immediate = true)
@Service
public class SendMailImpl implements SendMail {

    private static final String JCR_MAIL_SERVERS_PATH = "config/notifications/server";

    private static String MAIL_SMTP_HOST;
    private static String MAIL_SMTP_SOCKETFACTORY_PORT;
    private static String MAIL_SMTP_SOCKETFACTORY_CLASS;
    private static String MAIL_SMTP_AUTH;
    private static String MAIL_SMTP_PORT;
    private static String MAIL_SMTP_USERNAME;
    private static String MAIL_SMTP_PASSWORD;
    private static String MAIL_FROM;

    @Reference
    private SlingRepository repository;

    @Reference
    private LogService logger;

    private javax.jcr.Session jcrSession = null;

    protected void activate(ComponentContext context)  throws Exception {
        jcrSession = repository.loginAdministrative(null);
        readMailServerConfig();
    }
    protected void deactivate(ComponentContext componentContext) throws RepositoryException {
        if (jcrSession != null) {
            jcrSession.logout();
            jcrSession = null;
        }
    }

    private void readMailServerConfig() throws RepositoryException {
        Node storageNode = jcrSession.getRootNode().getNode(JCR_MAIL_SERVERS_PATH);
        MAIL_SMTP_HOST = storageNode.getProperty("mail.smtp.host").getValue().getString();
        MAIL_SMTP_SOCKETFACTORY_PORT = storageNode.getProperty("mail.smtp.socketFactory.port").getValue().getString();
        MAIL_SMTP_SOCKETFACTORY_CLASS = storageNode.getProperty("mail.smtp.socketFactory.class").getValue().getString();
        MAIL_SMTP_AUTH = storageNode.getProperty("mail.smtp.auth").getValue().getString();
        MAIL_SMTP_PORT = storageNode.getProperty("mail.smtp.port").getValue().getString();
        MAIL_SMTP_USERNAME = storageNode.getProperty("mail.smtp.username").getValue().getString();
        MAIL_SMTP_PASSWORD = storageNode.getProperty("mail.smtp.password").getValue().getString();
        MAIL_FROM = storageNode.getProperty("email.from").getValue().getString();
    }

    public boolean send(String title, String content, String emailTo) {
		Properties props = new Properties();
		props.put("mail.smtp.host", MAIL_SMTP_HOST);
		props.put("mail.smtp.socketFactory.port", MAIL_SMTP_SOCKETFACTORY_PORT);
		props.put("mail.smtp.socketFactory.class", MAIL_SMTP_SOCKETFACTORY_CLASS);
		props.put("mail.smtp.auth", MAIL_SMTP_AUTH);
		props.put("mail.smtp.port", MAIL_SMTP_PORT);

		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(MAIL_SMTP_USERNAME, MAIL_SMTP_PASSWORD);
				}
			});

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(MAIL_FROM));
			msg.setRecipients(Message.RecipientType.TO, parseMailAdresses(emailTo));
			msg.setSubject(title);
			msg.setText(content);

			Transport.send(msg);

		} catch (MessagingException e) {
			logger.log(LogService.LOG_ERROR, "Email using smtp host: " + MAIL_SMTP_HOST + ", didn't get through. Msg: " + e.getLocalizedMessage());
            return false;
		}

        return true;
    }

   /**
     * Parses a string list of email addresses separated by comma and validates email addresses.
     *
     * @param mailAddresses list of email addresses
     * @return array of InternetAddress
     */
    private InternetAddress[ ] parseMailAdresses(String mailAddresses) throws MessagingException {

        if (mailAddresses == null) {
//            logger.log(LogService.LOG_ERROR, "No email address has been provided.");
        }

        List<InternetAddress> addressList = new ArrayList<InternetAddress>();
        StringTokenizer st = new StringTokenizer(mailAddresses, ";, ");
        while (st.hasMoreTokens()) {
            String address = st.nextToken().trim();

            if (!isValidEmailAddress(address)) {
                throw new MessagingException("E-Mail Address '"+address+"' is not valid");
            }

            addressList.add(new InternetAddress(address));
        }

        if (addressList.size() == 0) {
            throw new MessagingException("No valid E-Mail Address has benn provided!");
        }

        return (InternetAddress[]) addressList.toArray(new InternetAddress[addressList.size()]);
    }

    /**
     * Basic check if an email address is valid.
     *
     * @param email the email address to check
     * @return true if valid
     */
    private static boolean isValidEmailAddress(String email) {
        String regex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)";
        return email.matches(regex);
    }
}
