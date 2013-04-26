package org.jboss.loom.migrators.deploymentScanner;

import org.jboss.loom.actions.AbstractStatefulAction;
import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.deploymentScanner.jaxb.StandaloneDeploymentScannerType;
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

/**
 * User: rsearls
 * Date: 4/18/13
 */
public class StandaloneDeploymentScannerAction extends AbstractStatefulAction{

    List<StandaloneDeploymentScannerType> dList =
        new ArrayList<StandaloneDeploymentScannerType>();
    File destFile;
    Document destDoc;
    Document rootNodeBackup = null;

    public StandaloneDeploymentScannerAction(File destFile, Document destDoc) {
        this.destFile = destFile;
        this.destDoc = destDoc;
    }

    public void addStandaloneDeploymentScannerType(
        StandaloneDeploymentScannerType sType){
        dList.add(sType);
    }

    public List<StandaloneDeploymentScannerType> getStandaloneDeploymentScannerTypeList(){
        return dList;
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
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (nList.getLength() == 0) {
                throw new MigrationException(
                    "deployment-scanner subsystem not found in file: "
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
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (nList.getLength() > 0) {

                JAXBContext jaxbCtx = JAXBContext.newInstance(
                    StandaloneDeploymentScannerType.class);
                Marshaller marshaller = jaxbCtx.createMarshaller();

                for (StandaloneDeploymentScannerType sType : dList) {

                    // transform data into DOM obj for insertion
                    Document tmpDoc = docBuilder.newDocument();
                    marshaller.marshal(sType, tmpDoc);

                    Node newChild = destDoc.adoptNode(
                        tmpDoc.getDocumentElement().cloneNode(true));
                    nList.item(0).appendChild(newChild);
                }

            } else {
                throw new MigrationException(
                    "deployment-scanner subsystem  element not found in file: "
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
            destDoc = rootNodeBackup;
        }

        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (!(nList.getLength() >= dList.size())){
                throw new MigrationException(
                    "new deployment-scanner elements not successfully added to the subsystem.");
            }

        } catch (XPathExpressionException xee) {
            throw new MigrationException(xee);
        }
    }


    @Override
    public void backup() throws MigrationException {

        // make a backup copy of the doc
        try {

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource xmlDomSource = new DOMSource(destDoc);
            DOMResult domResult = new DOMResult();
            transformer.transform(xmlDomSource, domResult);

            rootNodeBackup = (Document)domResult.getNode();

            setState(State.BACKED_UP);
        } catch (TransformerConfigurationException e) {
            System.out.println(e);
        } catch (TransformerException te) {
            System.out.println(te);
        }
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }

    @Override
    public String toDescription() {
        return "";
    }
}
