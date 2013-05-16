/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.ctx;

import org.jboss.loom.spi.IConfigFragment;

import java.util.LinkedList;
import java.util.List;

/**
 * Source server config data to be migrated.
 *
 * @author Roman Jakubco
 */
public class MigrationData {

    private List<IConfigFragment> configFragments = new LinkedList();


    @Override
    public String toString() {
        return "MigrationData{" + "configFragment=" + configFragments + '}';
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public List<IConfigFragment> getConfigFragments() {
        return configFragments;
    }
    //public void setConfigFragments(List<IConfigFragment> configFragment) { this.configFragments = configFragment; }
    //</editor-fold>

}// class
