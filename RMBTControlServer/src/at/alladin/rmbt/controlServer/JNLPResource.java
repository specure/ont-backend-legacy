package at.alladin.rmbt.controlServer;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class JNLPResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(JNLPResource.class);

    private static final String JNLP_FILENAME = "RMBTClient.jnlp";

    private static final String DEFAULT_URL = "nettest.org";

    // by default is turned on: QoS tests(-q), open result in browser(-o), verbose outputs(-v), GUI to see redirected outputs(-g)
    private static final String JNLP_FILE_CONTENT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<jnlp spec=\"1.0+\" codebase=\"https://nettest.org/jar\">\n" +
            "\t<information>\n" +
            "\t\t<title>RMBT Command Line Client</title>\n" +
            "\t\t<vendor>Specure GmbH</vendor>\n" +
            "\t\t<homepage href=\"https://nettest.org/\" />\n" +
            "\t\t<description>JAVA CLI Client</description>\n" +
            "\t</information>\n" +
            "\t<security>\n" +
            "\t\t<all-permissions/>\n" +
            "\t</security>\n" +
            "\t<resources>\n" +
            "\t\t<j2se version=\"1.7+\" />\n" +
            "\t\t<jar href=\"RMBTClient-1.0.jar\" />\n" +
            "\t</resources>\n" +
            "\t<application-desc name=\"RMBT Command Line Client\" main-class=\"at.alladin.rmbt.client.RMBTClientRunner\">\n" +
            "\t\t<argument>-v</argument>\n" +
            "\t\t<argument>-o</argument>\n" +
            "\t\t<argument>-q</argument>\n" +
            "\t\t<argument>-g</argument>\n" +
            "\t\t<argument>-h=%s</argument>\n" +
            "\t\t<argument>-p=443</argument>\n" +
//            "\t\t<argument>-legacy=true</argument>\n" +
            " \t </application-desc>\n" +
            "</jnlp>";

    @Post("json")
    public Representation request(String entity) {

        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        try {

            // generate JNLP file content on the fly
            final OutputRepresentation result = new OutputRepresentation(MediaType.APPLICATION_JNLP) {

                @Override
                public void write(OutputStream out) throws IOException {
                    try {
                        // get url
                        String url = settings.getString("RMBT_URL");

                        // check url
                        if (url == null || url.isEmpty()) {
                            url = DEFAULT_URL;
                        }

                        OutputStreamWriter osw = new OutputStreamWriter(out);

                        // add url to content
                        String jnlpFileContent = String.format(JNLP_FILE_CONTENT, url);
                        logger.debug("JNLP: " + jnlpFileContent);

                        // write content
                        osw.write(jnlpFileContent);

                        // flush
                        osw.flush();

                        // close stream
                        osw.close();

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            };

            // set type
            Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);

            // set filename
            disposition.setFilename(JNLP_FILENAME);
            result.setDisposition(disposition);

            // log response
            logger.debug("rsponse: " + result.toString());

            // return file
            return result;

        } catch (final Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Get("json")
    public Representation retrieve(final String entity) {
        return request(entity);
    }

}
