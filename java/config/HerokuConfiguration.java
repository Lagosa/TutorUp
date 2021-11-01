package itreact.tutorup.server.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for set the configuration when application runs on Heroku.
 *
 */
class HerokuConfiguration extends SimpleConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(HerokuConfiguration.class);

    @Override
    protected void initConfiguration() {
        // overwrite DB values
        String uriValue = System.getenv("DATABASE_URL");
		System.out.println("Heroku DATABASE_URL=" + uriValue);
        // this is for e-mail part and other config files
        super.initConfiguration();
        try {
            URI dbUri = new URI(uriValue);
            jdbcUsername = dbUri.getUserInfo().split(":")[0];
            jdbcPassword = dbUri.getUserInfo().split(":")[1];
            jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        } catch (URISyntaxException e) {
            LOG.error("Could not process Heroku configuration URL: {}", uriValue, e);
            throw new IllegalStateException("Could not handle Heroku configuration value", e);
        }

    }
}
