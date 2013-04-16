package cz.muni.fi.jboss.migration.migrators.deploymentScanner;

import cz.muni.fi.jboss.migration.AbstractMigrator;
import cz.muni.fi.jboss.migration.MigrationContext;
import cz.muni.fi.jboss.migration.MigrationData;
import cz.muni.fi.jboss.migration.conf.AS5Config;
import cz.muni.fi.jboss.migration.conf.GlobalConfiguration;
import cz.muni.fi.jboss.migration.ex.ApplyMigrationException;
import cz.muni.fi.jboss.migration.ex.CliScriptException;
import cz.muni.fi.jboss.migration.ex.LoadMigrationException;
import cz.muni.fi.jboss.migration.ex.NodeGenerationException;
import cz.muni.fi.jboss.migration.migrators.deploymentScanner.jaxb.ListType;
import cz.muni.fi.jboss.migration.migrators.deploymentScanner.jaxb.ValueType;
import cz.muni.fi.jboss.migration.utils.Utils;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: rsearls
 * Date: 4/15/13
 */
public class DeploymentScannerMigrator extends AbstractMigrator {

    private static final Logger log = LoggerFactory.getLogger(DeploymentScannerMigrator.class);


    public DeploymentScannerMigrator(GlobalConfiguration globalConfig,
                                     MultiValueMap config) {
        super(globalConfig, config);

    }

    @Override
    protected String getConfigPropertyModuleName() {
        return "scanner???";//"datasource";
    }

    // step 1
    @Override
    public void loadAS5Data(MigrationContext ctx) throws LoadMigrationException{

        AS5Config  as5Config = super.getGlobalConfig().getAS5Config();
        /**
        File f = Utils.createPath(as5Config.getDir(), "server",
            as5Config.getProfileName(),"deploy", "hdscanner-jboss-beans.xml");
        **/
        try {
            File f = Utils.createPath(as5Config.getDir(), "server",
                as5Config.getProfileName(), "conf/bootstrap", "profile.xml");

            if (f.exists() && f.canRead()) {
                List<ValueType> valueList = getData(f);
                MigrationData mData = new MigrationData();
                mData.getConfigFragments().addAll(valueList);
                ctx.getMigrationData().put(this.getClass(), mData);

            } else {
                throw new LoadMigrationException("Cannot find/open file: " +
                    f.getAbsolutePath(), new FileNotFoundException());
            }
        } catch (Exception e) { //TODO fix this
          System.out.println(e);
        }
    }

    /**
     *
     * @param f
     * @return
     */
    private List<ValueType> getData(File f){

        List<ValueType> resultList = new ArrayList<ValueType>();

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document doc = docBuilder.parse(f);

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean[@name='BootstrapProfileFactory']/property[@name='applicationURIs']//list[@elementClass='java.net.URI']";
            Node  n = (Node) xpath.evaluate(exp, doc, XPathConstants.NODE);

            Unmarshaller unmarshaller = JAXBContext.newInstance(
                ListType.class).createUnmarshaller();
            ListType l = (ListType) unmarshaller.unmarshal(n);


            //String URL_PREFIX = "file://";

            for (ValueType v : l.getValue()) {
                String value = v.getValue().trim();
                //System.out.println("list value: " + value);

                if (v.isExternalDir() /*value.startsWith(URL_PREFIX)*/) {
                    //String absPath = value.substring(URL_PREFIX.length());
                    //System.out.println("external path: " + absPath);
                    resultList.add(v);
                }
            }

        } catch (JAXBException e) {  //TODO: fix these
            System.out.println(e);
        } catch (ParserConfigurationException cpe) {
            //throw new RuntimeException(ex);
            System.out.println(cpe);
        } catch (SAXException saxe) {
            System.out.println(saxe);
        } catch (IOException ioe) {
            System.out.println(ioe);
        } catch (XPathExpressionException pee) {
            System.out.println(pee);
        }
        return resultList;
    }


    // step 2
    @Override
    public void createActions( MigrationContext ctx ){

    }


    @Override
    public void apply(MigrationContext ctx) throws ApplyMigrationException{
        /**
        try {
            Document doc = ctx.getAS7ConfigXmlDoc();
            NodeList subsystems = doc.getElementsByTagName("subsystem");
            for (int i = 0; i < subsystems.getLength(); i++) {
                if (!(subsystems.item(i) instanceof Element)) {
                    continue;
                }
                if (((Element) subsystems.item(i)).getAttribute("xmlns").contains("deployment-scanner")) {
                    Node parent = subsystems.item(i);
                    Node lastNode = parent.getLastChild();
                    Node firstNode = parent.getFirstChild();
                }
            }
        } catch (Exception e) {
            throw new ApplyMigrationException(e);
        }
        **/
    }

    @Override
    public List<Node> generateDomElements(MigrationContext ctx)
        throws NodeGenerationException{
        return null;
    }


    //@Override
    public List<String> generateCliScripts(MigrationContext ctx)
        throws CliScriptException{
        return null;
    }
}
