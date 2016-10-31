package csc.journal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class App
{
    private static final String WEBAPP_DIR = "/webapp/";

    public static void main(String[] args) throws Exception
    {
        final int port = System.getenv("PORT") == null || System.getenv("PORT").isEmpty()
                        ? 8080
                        : Integer.valueOf(System.getenv("PORT"));

        App app = new App(port);
        app.start();
        app.waitForInterrupt();
    }

    private final int port;
    private final Server server;

    public App(int port)
    {
        this.port = port;
        this.server = new Server();
    }

    public void start() throws Exception
    {
        final ServerConnector connector = connector();
        server.addConnector(connector);

        final URI baseUri = getWebRootResourceUri();

        final WebAppContext webAppContext = getWebAppContext(baseUri);

        server.setHandler(webAppContext);

        server.start();
    }

    private ServerConnector connector()
    {
        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        
        return connector;
    }

    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException
    {
        final URL indexUri = this.getClass().getResource(WEBAPP_DIR);
        if (indexUri == null)
        {
            throw new FileNotFoundException("Unable to find resource " + WEBAPP_DIR);
        }

        return indexUri.toURI();
    }

    private WebAppContext getWebAppContext(URI baseUri)
    {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(baseUri.toASCIIString());

        // Add Application Servlets
        context.addServlet(JournalServlet.class, "/journal/*");
        context.addServlet(defaultServletHolder(baseUri), "/");

        return context;
    }

    private ServletHolder defaultServletHolder(URI baseUri)
    {
        final ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed", "true");

        return holderDefault;
    }

    public void stop() throws Exception
    {
        server.stop();
    }

    public void waitForInterrupt() throws InterruptedException
    {
        server.join();
    }
}
