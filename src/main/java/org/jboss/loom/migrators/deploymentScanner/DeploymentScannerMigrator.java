package org.jboss.loom.migrators.deploymentScanner;

import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.CliApiCommandBuilder;
import org.jboss.loom.MigrationContext;
import org.jboss.loom.MigrationData;
import org.jboss.loom.actions.CliCommandAction;
import org.jboss.loom.conf.AS5Config;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jboss.loom.migrators.deploymentScanner.jaxb.ListType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.PropertyType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.StandaloneDeploymentScannerType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.Subsystem;
import org.jboss.loom.migrators.deploymentScanner.jaxb.ValueType;

/**
 * User: rsearls
 * Date: 4/15/13
 */
public class DeploymentScannerMigrator extends AbstractMigrator {

    private static final Logger log = LoggerFactory.getLogger(
        DeploymentScannerMigrator.class);


    public DeploymentScannerMigrator(GlobalConfiguration globalConfig,
                                     MultiValueMap config) {
        super(globalConfig, config);

    }

    @Override
    protected String getConfigPropertyModuleName() {
        return "deployment-scanner";
    }

    // step 1
    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException {

        AS5Config as5Config = super.getGlobalConfig().getAS5Config();
        int scanPeriod = getScanPeriod(as5Config);

        File f = Utils.createPath(as5Config.getDir(), "server",
            as5Config.getProfileName(), "conf/bootstrap", "profile.xml");

        if (f.exists() && f.canRead()) {
            List<ValueType> valueList = getDeploymentDirs(f);
            MigrationData mData = new MigrationData();
            mData.getConfigFragments().addAll(valueList);
            ctx.getMigrationData().put(this.getClass(), mData);

            for (ValueType v : valueList) {
                v.setScanPeriod(scanPeriod);
            }
        } else {
            throw new LoadMigrationException("Cannot find/open file: " +
                f.getAbsolutePath(), new FileNotFoundException());
        }
    }

    // step 2
    @Override
    public void createActions( MigrationContext ctx ){

        File as7configFile = new File(getGlobalConfig().getAS7Config().getConfigFilePath());

        try {

            Document destDoc = ctx.getAS7ConfigXmlDoc();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc,
                XPathConstants.NODESET);

            if (0 == nList.getLength()) {
                // No deployment-scanner subsystem found.  Prep to add

                Subsystem subsystem = createDeploymentScannerSubsystem(
                    destDoc, ctx, xpath);

                if (subsystem != null) {
                    SubsystemAction action = new SubsystemAction(subsystem,
                        as7configFile, ctx.getAS7ConfigXmlDoc());
                    ctx.getActions().add(action);
                }
            } else {

                // deployment-scanner subsystem exists.  Prep to add element

                StandaloneDeploymentScannerAction sAction =
                    new StandaloneDeploymentScannerAction(as7configFile,
                        ctx.getAS7ConfigXmlDoc());

                for (IConfigFragment fragment : ctx.getMigrationData().get(
                    DeploymentScannerMigrator.class).getConfigFragments()) {

                    StandaloneDeploymentScannerType destDScanner =
                        new StandaloneDeploymentScannerType((ValueType)fragment);
                    sAction.addStandaloneDeploymentScannerType(destDScanner);

                    //- create CLI cmds
                    ModelNode mNode = createModelNode((ValueType)fragment, ctx);
                    //ctx.getActions().add(new CliCommandAction(
                    //    DeploymentScannerMigrator.class, "create deployment-scanner", mNode));

                    //- workaround .. force tools not to fail with null exception.
                    ctx.getBatch().add(
                        new org.jboss.as.cli.batch.impl.DefaultBatchedCommand("create deployment-scanner",  mNode));
                }

                if (!sAction.getStandaloneDeploymentScannerTypeList().isEmpty()){
                    ctx.getActions().add(sAction);
                }
            }

        } catch (JAXBException e) {
            System.out.println(e);
        } catch(XPathExpressionException xee) {
            System.out.println(xee);
        }
    }

    /**
     *
     * @param fragment
     * @return
     */
    private ModelNode createModelNode(ValueType fragment, MigrationContext ctx){

        StringBuilder sb = new StringBuilder();
        sb.append("/subsystem=deployment-scanner/scanner=");
        sb.append(Long.toString((new Date()).getTime()));
        sb.append("/:add(path=");
        sb.append(fragment.getDeployPath());
        sb.append(",scan-interval=");
        sb.append(fragment.getScanPeriod());
        sb.append(")");

        ModelNode mNode = new ModelNode();
        mNode.set(sb.toString());
        //System.out.println("ModelNode: " + mNode.asString());  // debug

        //-------
        ModelNode connDefCmd = new ModelNode();
        connDefCmd.get(ClientConstants.OP).set(ClientConstants.ADD);
        connDefCmd.get(ClientConstants.OP_ADDR).add("subsystem", "deployment-scanner");
        connDefCmd.get(ClientConstants.OP_ADDR).add("scanner", Long.toString((new Date()).getTime()));

        CliApiCommandBuilder builder = new CliApiCommandBuilder(connDefCmd);

        builder.addProperty("path", fragment.getDeployPath());
        Integer scanPeriod = new Integer(fragment.getScanPeriod());
        builder.addProperty("scan-interval", scanPeriod.toString());

        //System.out.println("connDefCmd: asString:" + connDefCmd.asString());
        //System.out.println("connDefCmd: string:" + connDefCmd.toString());

        ctx.getActions().add(new CliCommandAction(DeploymentScannerMigrator.class,
            mNode.asString(), builder.getCommand()));

        return mNode;
    }


    /* --------------------------------------------------------------------*/
    /* --------------------------------------------------------------------*/
    /* --------------------------------------------------------------------*/

    private Subsystem createDeploymentScannerSubsystem(Document destDoc,
        MigrationContext ctx, XPath xpath)
        throws JAXBException, XPathExpressionException {

        //deployment-scanner subsystem does not exist.  Create it.
        String exp = "/server/profile";
        NodeList pList = (NodeList) xpath.evaluate(exp, destDoc,
            XPathConstants.NODESET);
        Subsystem subsystem = null;

        if (pList.getLength() > 0) {
            subsystem = new Subsystem();

            for (IConfigFragment fragment : ctx.getMigrationData().get(
                DeploymentScannerMigrator.class).getConfigFragments()) {

                // transfer data from prev to current version
                StandaloneDeploymentScannerType destDScanner =
                    new StandaloneDeploymentScannerType((ValueType) fragment);
                subsystem.getDeploymentScanner().add(destDScanner);
            }
        }
        return subsystem;
    }

    /**
     *  getDeploymentDirs
     * @param f
     * @return
     */
    private List<ValueType> getDeploymentDirs(File f) throws LoadMigrationException {

        List<ValueType> resultList = new ArrayList<ValueType>();

        try {

            DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();
            Document doc = docBuilder.parse(f);

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean[@name='BootstrapProfileFactory']/property[@name='applicationURIs']//list[@elementClass='java.net.URI']";
            Node  n = (Node) xpath.evaluate(exp, doc, XPathConstants.NODE);

            Unmarshaller unmarshaller = JAXBContext.newInstance(
                ListType.class).createUnmarshaller();
            ListType l = (ListType) unmarshaller.unmarshal(n);


            for (ValueType v : l.getValue()) {

                if (v.isExternalDir()) {
                    resultList.add(v);
                }
            }

        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (SAXException saxe) {
            throw new LoadMigrationException(saxe);
        } catch (IOException ioe) {
            throw new LoadMigrationException(ioe);
        } catch (XPathExpressionException xee) {
            throw new LoadMigrationException(xee);
        }
        return resultList;
    }

    /**
     *
     * @param as5Config
     * @return
     */
    private int getScanPeriod(AS5Config  as5Config) throws LoadMigrationException {

        int result = 5000;  // AS5 default value

        try {

            File f = Utils.createPath(as5Config.getDir(), "server",
                as5Config.getProfileName(), "deploy", "hdscanner-jboss-beans.xml");

            DocumentBuilder docBuilder = Utils.createXmlDocumentBuilder();
            Document doc = docBuilder.parse(f);


            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean/property[@name='scanPeriod']";
            Node node = (Node) xpath.evaluate(exp, doc, XPathConstants.NODE);

            if (node != null) {

                JAXBContext jaxbCtx = JAXBContext.newInstance(PropertyType.class);
                Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();

                PropertyType pType = (PropertyType) unmarshaller.unmarshal(node);
                List<Serializable> contentList = pType.getContent();
                if (contentList.size() > 0) {
                    Serializable s = contentList.get(0);
                    if (s instanceof String){
                        result = Integer.parseInt((String)s);
                        System.out.println("scanPeriod: " + result);
                    }
                }
            }

        } catch (JAXBException e) {
            throw new LoadMigrationException(e);
        } catch (SAXException saxe) {
            throw new LoadMigrationException(saxe);
        } catch (IOException ioe) {
            throw new LoadMigrationException(ioe);
        } catch (XPathExpressionException xee) {
            throw new LoadMigrationException(xee);
        }
        return result;
    }

}
