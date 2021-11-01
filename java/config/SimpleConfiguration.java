package itreact.tutorup.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a simple configuration that reads it's properties from
 * a file. The location of the file is provided as System property.
 *
 */
class SimpleConfiguration implements Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleConfiguration.class);

    // JDBC connection data
    protected String jdbcUrl;
    protected String jdbcUsername;
    protected String jdbcPassword;

    protected String emailFrom;
    protected String gmailUsername;
    protected String gmailPassword;

    protected String siteUrl;

    SimpleConfiguration() {
        initConfiguration();
    }

    /**
     * Init configuration from config file.
     */
    protected void initConfiguration() {
        try {
            String configFile = "/db/" + System.getProperty("blank.env", "prod") + ".config";
            LOG.info("Config file: {}", configFile);
            InputStream is = getClass().getResourceAsStream(configFile);
            Properties props = new Properties();
            props.load(is);
            is.close();

            jdbcUrl = props.getProperty("db.url");
            jdbcUsername = props.getProperty("db.username");
            jdbcPassword = props.getProperty("db.password");

            emailFrom = props.getProperty("email.from");
            gmailUsername = props.getProperty("email.username");
            gmailPassword = props.getProperty("email.password");

            siteUrl = props.getProperty("site.url");
        } catch (IOException e) {
            LOG.error("Error loading configuration", e);
            throw new IllegalStateException("Could not read configuration", e);
        }
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getJdbcUsername() {
        return jdbcUsername;
    }

    @Override
    public String getJdbcPassword() {
        return jdbcPassword;
    }

    @Override
    public String getEmailFrom() {
        return emailFrom;
    }

    @Override
    public String getGmailUsername() {
        return gmailUsername;
    }

    @Override
    public String getGmailPassword() {
        return gmailPassword;
    }

    @Override
    public String getSiteUrl() {
        return siteUrl;
    }

}
