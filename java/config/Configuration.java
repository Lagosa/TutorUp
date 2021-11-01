package itreact.tutorup.server.config;

/**
 * This interface contains the configuration elements necessary to run the
 * application.
 *
 */
public interface Configuration {
    /**
     * Gets the JDBC URL used for connecting to database.
     *
     * @return the URL for connecting to database
     */
    String getJdbcUrl();

    /**
     * Gets the database username
     *
     * @return the username of the database we should use to connect
     */
    String getJdbcUsername();

    /**
     * Gets the password corresponding to database username
     *
     * @return the database password
     */
    String getJdbcPassword();

    /**
     * Gets the e-mail address used in FROM field of e-mails
     *
     * @return the e-mail FROM
     */
    String getEmailFrom();

    /**
     * Gets the GMail username used for sending e-mails.
     *
     * @return the GMail username
     */
    String getGmailUsername();

    /**
     * Gets the GMail password corresponding to username
     *
     * @return the password
     */
    String getGmailPassword();

    /**
     * Gets the site URL prefix
     *
     * @return the site URL prefix
     */
    String getSiteUrl();
}
