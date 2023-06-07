/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.statisticServer.export;

import org.restlet.data.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

public final class ExportXMLResource extends ExportResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ExportXMLResource.class);

    @Override
    protected MediaType getMediaType() {
        return MediaType.TEXT_XML;
    }


    @Override
    protected String getFileName() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%.xml";
    }

    @Override
    protected String getFileNameCurrent() {
        return FILENAME_PREFIX + ".xml";
    }

    @Override
    protected String getFileNameZip() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%-xml.zip";
    }

    @Override
    protected String getFileNameZipCurrent() {
        return FILENAME_PREFIX + "-xml.zip";
    }

    @Override
    protected Boolean getZipped() {
        return true;
    }

    @Override
    void writeToFile(OutputStream os, ExportData exportData) throws IOException {

        logger.debug("Started writing to " + getFileName());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error(e.getMessage());
        }
        Document doc = dBuilder.newDocument();

        // root element
        Element rootElement = doc.createElement("tests");
        doc.appendChild(rootElement);

        // iterate through all data
        for (int i = 0; i < exportData.data.size(); i++) {

            // create child element
            Element testElement = doc.createElement("test");
            rootElement.appendChild(testElement);

//            // iterate through all columns
//            for (int j = 0; j < exportData.columns.length; j++) {
//                // setting attribute to element
//                Attr attr = doc.createAttribute(exportData.columns[j]);
//                attr.setValue(exportData.data.get(i)[j]);
//                testElement.setAttributeNode(attr);
//            }

            // iterate through all columns
            for (int j = 0; j < exportData.columns.length; j++) {
                // setting attribute to element
                Element element = doc.createElement(exportData.columns[j]);
                element.setTextContent(exportData.data.get(i)[j]);
                testElement.appendChild(element);
            }

        }

        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");

            trans.transform(new DOMSource(doc), new StreamResult(os));

        } catch (TransformerException e) {
            logger.error(e.getMessage());
        }

        logger.debug("Finished writing to " + getFileName());
    }

}
