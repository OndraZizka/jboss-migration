/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jboss.loom.conf.AS5Config;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.GlobalConfiguration;
import org.jboss.loom.ctx.MigrationContext;
import org.jboss.loom.ctx.MigratorData;
import org.jboss.loom.ex.LoadMigrationException;
import org.jboss.loom.migrators.AbstractMigrator;
import org.jboss.loom.migrators.deploymentScanner.jaxb.ListType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.PropertyType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.StandaloneDeploymentScannerType;
import org.jboss.loom.migrators.deploymentScanner.jaxb.Subsystem;
import org.jboss.loom.migrators.deploymentScanner.jaxb.ValueType;
import org.jboss.loom.spi.IConfigFragment;
import org.jboss.loom.utils.Utils;
import org.jboss.loom.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * User: rsearls
 * Date: 4/15/13
 */
public class DeploymentScannerMigrator extends AbstractMigrator {

    private static final Logger log = LoggerFactory.getLogger( DeploymentScannerMigrator.class );

    public DeploymentScannerMigrator(GlobalConfiguration globalConfig) {
        super(globalConfig);
    }

    @Override protected String getConfigPropertyModuleName() { return "deployment-scanner"; }

    
    // step 1
    @Override
    public void loadSourceServerConfig(MigrationContext ctx) throws LoadMigrationException {

        AS5Config as5Config = super.getGlobalConfig().getAS5Config();
        int scanPeriod = getScanPeriod(as5Config);

        File f = Utils.createPath(as5Config.getConfDir(), "bootstrap/profile.xml");

        if( f.exists() && f.canRead() ) {
            List<ValueType> valueList = getDeploymentDirs(f);

            valueList = checkDestinationPath(valueList);
            valueList = checkSourcePath(valueList);

            MigratorData mData = new MigratorData();
            mData.getConfigFragments().addAll(valueList);
            ctx.getMigrationData().put(this.getClass(), mData);

            for (ValueType v : valueList) {
                v.setScanPeriod(scanPeriod);
            }
        } else {
            throw new LoadMigrationException("Cannot find/open file: " + f.getAbsolutePath());
        }
    }


    /**
     * Check the destination subsystem entries for duplicate directory names.
     * Remove the dup ones from the create list.
     *
     * @param valueList
     * @return
     * @throws LoadMigrationException
     */
    private List<ValueType> checkDestinationPath(List<ValueType> valueList)
        throws LoadMigrationException {

        AS7Config as7Config = super.getGlobalConfig().getAS7Config();

        File f = new File(as7Config.getConfigFilePath());
        if( ! (f.exists() && f.canRead() ) )
            return valueList;
        
        try {
            DocumentBuilder docBuilder = XmlUtils.createXmlDocumentBuilder();
            Document doc = docBuilder.parse(f);

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nodeList = (NodeList) xpath.evaluate(exp, doc, XPathConstants.NODESET );

            int cnt = nodeList.getLength();
            for( int i =0; i < cnt; i++ ){
                Element node = (Element) nodeList.item(i);
                String tmpPath = node.getAttribute("path");

                List<ValueType> removeList = new LinkedList();
                for (ValueType v: valueList){
                    if (tmpPath.equals(v.getDeployPath()))
                        removeList.add(v);
                }

                // remove the duplicates
                for (ValueType v: removeList)
                    valueList.remove(v);
            }
        }
        catch ( SAXException | IOException | XPathExpressionException saxe) {
            throw new LoadMigrationException(saxe);
        }
        return valueList;
    }


    private List<ValueType> checkSourcePath(List<ValueType> valueList) {

        List<ValueType> notFoundList = new LinkedList();
        for (ValueType v : valueList) {
            File f = new File(v.getDeployPath());
            if( !f.exists() )
                notFoundList.add(v);
        }

        // TBC: Why not remove directly?
        for( ValueType v : notFoundList){
            valueList.remove(v);
        }
        return valueList;
    }


    // step 2
    @Override
    public void createActions( MigrationContext ctx ){

        File as7configFile = new File(getGlobalConfig().getAS7Config().getConfigFilePath());

        try {
            Document destDoc = ctx.getAS7ConfigXmlDoc();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc, XPathConstants.NODESET);

            if (0 == nList.getLength()) {
                // No deployment-scanner subsystem found.  Prep to add
                Subsystem subsystem = createDeploymentScannerSubsystem( destDoc, ctx, xpath );
                if( subsystem != null ) {
                    SubsystemAction action = new SubsystemAction(subsystem, as7configFile, ctx.getAS7ConfigXmlDoc());
                    ctx.getActions().add(action);
                }
            } 
            else {
                // deployment-scanner subsystem exists.  Prep to add element

                // TODO: This needs to be done using management API.
                StandaloneDeploymentScannerAction sAction =
                    new StandaloneDeploymentScannerAction(as7configFile, ctx.getAS7ConfigXmlDoc());

                for (IConfigFragment fragment : ctx.getMigrationData().get(
                    DeploymentScannerMigrator.class).getConfigFragments()) {

                    StandaloneDeploymentScannerType destDScanner =
                        new StandaloneDeploymentScannerType((ValueType)fragment);
                    sAction.addStandaloneDeploymentScannerType(destDScanner);

                    //- create CLI cmds
                    String cliCmdStr = createCliCmdStr((ValueType)fragment);
                    sAction.addWarning(cliCmdStr);
                }

                if (!sAction.getStandaloneDeploymentScannerTypeList().isEmpty()){
                    ctx.getActions().add(sAction);
                }
            }

        } catch( XPathExpressionException| JAXBException e) {
            // TODO: Throw something.
            log.error(e.toString());
        }
    }

    private String createCliCmdStr(ValueType fragment){

        StringBuilder sb = new StringBuilder();
        sb.append("/subsystem=deployment-scanner/scanner=");
        sb.append(Long.toString((new Date()).getTime()));
        sb.append("/:add(path=");
        sb.append(fragment.getDeployPath());
        sb.append(",scan-interval=");
        sb.append(fragment.getScanPeriod());
        sb.append(")");

        return sb.toString();
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

        List<ValueType> resultList = new LinkedList();

        try {
            DocumentBuilder docBuilder = XmlUtils.createXmlDocumentBuilder();
            Document doc = docBuilder.parse(f);

            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean[@name='BootstrapProfileFactory']/property[@name='applicationURIs']//list[@elementClass='java.net.URI']";
            Node  n = (Node) xpath.evaluate(exp, doc, XPathConstants.NODE);

            Unmarshaller unmarshaller = JAXBContext.newInstance(ListType.class).createUnmarshaller();
            ListType l = (ListType) unmarshaller.unmarshal(n);

            for (ValueType v : l.getValue()) {
                if (v.isExternalDir())
                    resultList.add(v);
            }
        }
        catch (JAXBException | SAXException | IOException | XPathExpressionException e) {
            throw new LoadMigrationException(e);
        }
        return resultList;
    }

    /**
     *
     * @param as5Config
     * @return
     */
    private int getScanPeriod(AS5Config as5Config) throws LoadMigrationException {

        int result = 5000;  // AS5 default value

        try {
            File f = Utils.createPath(as5Config.getDeployDir(), "hdscanner-jboss-beans.xml");

            DocumentBuilder docBuilder = XmlUtils.createXmlDocumentBuilder();
            Document doc = docBuilder.parse(f);

            // Get the data into a JAXB bean.
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/deployment/bean/property[@name='scanPeriod']";
            Node node = (Node) xpath.evaluate(exp, doc, XPathConstants.NODE);
            if( node == null )
                return result;
            JAXBContext jaxbCtx = JAXBContext.newInstance(PropertyType.class);
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            PropertyType pType = (PropertyType) unmarshaller.unmarshal(node);
            
            List<Serializable> contentList = pType.getContent();
            if( contentList.isEmpty() ) 
                return result;
            
            Serializable s = contentList.get(0);
            if( s instanceof String ){
                result = Integer.parseInt((String)s);
                log.trace("scanPeriod: " + result);
            }
        }
        catch (JAXBException | SAXException | IOException | XPathExpressionException e) {
            throw new LoadMigrationException(e);
        }
        return result;
    }

}// class
