package itreact.tutorup.server.config;
/**
 * This class is used for obtaining {@link Configuration} instance.
 *
 */
public class ConfigurationFactory {

    private static Configuration configuration = null;

    /**
     * Gets the {@link Configuration} instance
     *
     * @return the configuration instance
     */
    public static synchronized Configuration getInstance() {
        if (configuration == null) {
            initConfiguration();
        }
        return configuration;
    }

    /**
     * Init the configuration class depending on environment.
     */
    private static void initConfiguration() {
    	System.out.println("Found JDBC_DATABASE_URL=" + System.getenv("JDBC_DATABASE_URL") );
        if (System.getenv("JDBC_DATABASE_URL") != null) {
            configuration = new HerokuConfiguration();
        } else {
            configuration = new SimpleConfiguration();
        }
    }
}

