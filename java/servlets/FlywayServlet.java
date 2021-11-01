package itreact.tutorup.server.servlets;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import itreact.tutorup.server.db.DatabaseFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet used for initializing Flyway and perform data migrations
 *
 */
public class FlywayServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(FlywayServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        LOG.info("Initializing Flyway...");
        Flyway flyway = new Flyway();
        flyway.setDataSource(DatabaseFactory.getConnectionManager().getDataSource());

        if (!flyway.isBaselineOnMigrate()) {
            LOG.info("Performing baseline...");
            try {
            	flyway.baseline();
            } catch (Exception e) {
            	LOG.error("", e);
            	throw e;
            }
            LOG.info("After baseline performed");
        }
        flyway.repair();
        int scripts = flyway.migrate();
        LOG.info("Flyway init completed. {} scripts were applied.", scripts);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        String source = req.getParameter("source");

        resp.getOutputStream().print(source);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
