package org.jboss.loom.migrators.deploymentScanner;

import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.deploymentScanner.jaxb.Subsystem;
import org.jboss.loom.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * User: rsearls
 * Date: 4/18/13
 */
public class SubsystemAction extends AbstractStatefulAction {

    File destFile;
    Document destDoc;
    Subsystem subsystem;
    Node rootNodeBackup = null;

    public  SubsystemAction (){
    }


    public SubsystemAction(Subsystem subsystem, File destFile, Document destDoc) {
        this.subsystem = subsystem;
        this.destFile = destFile;
        this.destDoc = destDoc;
    }

    @Override
    public void preValidate() throws MigrationException {

        if (destFile == null || !destFile.exists()){
            throw new MigrationException(
                "Destination configuration file " +
                    ((destFile == null)? "name is NULL." : destFile.getAbsolutePath() + " is not found."));
        } else if (!destFile.canWrite()){
            throw new MigrationException("No write permissions for file "
                + destFile.getAbsolutePath());
        }

        // confirm required xml element
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile";
            NodeList pList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (pList.getLength() == 0) {
                throw new MigrationException("profile element not found in file: "
                    + destDoc.getBaseURI());
            }
        } catch (XPathExpressionException e) {
            throw new MigrationException(e);
        }
    }


    @Override
    public void perform() throws MigrationException {

        try {

            DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile";
            NodeList pList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (pList.getLength() > 0) {

                JAXBContext jaxbCtx = JAXBContext.newInstance(Subsystem.class);
                Marshaller marshaller = jaxbCtx.createMarshaller();

                // transform data into DOM obj for insertion
                Document tmpDoc = docBuilder.newDocument();
                marshaller.marshal(subsystem, tmpDoc);

                Node newChild = destDoc.adoptNode(
                    tmpDoc.getDocumentElement().cloneNode(true));
                pList.item(0).appendChild(newChild);

            } else {
                throw new MigrationException("profile element not found in file: "
                    + destDoc.getBaseURI());
            }

            setState(State.DONE);

        } catch (JAXBException e) {
            throw new MigrationException(e);
        } catch(XPathExpressionException xee) {
            throw new MigrationException(xee);
        }
    }


    @Override
    public void rollback() throws MigrationException {

        if (rootNodeBackup == null) {
            throw new MigrationException("No backup data to rollback to.");
        } else {
            Node rootNode = (Node)destDoc;
            rootNode.replaceChild(rootNodeBackup, rootNode);
        }
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList pList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (pList.getLength() == 0){
                throw new MigrationException(
                    "new deployment-scanner subsystem not successfully created");
            }

        } catch (XPathExpressionException xee) {
            throw new MigrationException(xee);
        }
    }


    @Override
    public void backup() throws MigrationException {
        rootNodeBackup = ((Node)destDoc).cloneNode(true);
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }

    @Override
    public String toDescription() {
        return "Create a new deployment-scanner subsystem with a reference to the user defined AS5 deployment directory.";
    }
}
