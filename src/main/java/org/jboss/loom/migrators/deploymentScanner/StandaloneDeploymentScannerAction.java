/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.migrators.deploymentScanner;

import org.jboss.loom.ex.MigrationException;
import org.jboss.loom.migrators.deploymentScanner.jaxb.StandaloneDeploymentScannerType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.jboss.loom.actions.ManualAction;

/**
 * TODO: Javadoc.
 * 
 * @author: rsearls
 */
public class StandaloneDeploymentScannerAction extends ManualAction {

    List<StandaloneDeploymentScannerType> dList = new LinkedList();
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
            throw new MigrationException( "Destination configuration file " +
                    ((destFile == null)? "name is NULL." : destFile.getAbsolutePath() + " is not found."));
        }
        else if (!destFile.canWrite()){
            throw new MigrationException("No write permissions for file " + destFile.getAbsolutePath());
        }

        // confirm required xml element
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String exp = "/server/profile/subsystem/deployment-scanner";
            NodeList nList = (NodeList) xpath.evaluate(exp, destDoc, XPathConstants.NODESET);

            if( nList.getLength() == 0 )
                throw new MigrationException("deployment-scanner subsystem not found in file: " + destDoc.getBaseURI());
        }
        catch( XPathExpressionException e ) {
            throw new MigrationException(e);
        }
    }


    @Override
    public void perform() throws MigrationException {
        setState(State.DONE);
    }


    @Override
    public void rollback() throws MigrationException {
        setState(State.ROLLED_BACK);
    }


    @Override
    public void postValidate() throws MigrationException {
    }


    @Override
    public void backup() throws MigrationException {
        setState(State.BACKED_UP);
    }


    @Override
    public void cleanBackup() {
        setState(State.FINISHED);
    }
}
