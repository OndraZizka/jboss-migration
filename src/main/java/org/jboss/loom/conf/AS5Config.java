/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.conf;

import java.io.File;
import org.jboss.loom.utils.Utils;

/**
 * AS 5 specific configuration.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class AS5Config {

    private String as5dir;
    private String as5profileName = "default";
    public static final String AS5_PROFILES_DIR = "server";
    public static final String AS5_DEPLOY_DIR = "deploy";
    public static final String AS5_CONF_DIR = "conf";

    /**  Server directory of AS 5 - "$AS5dir/server". This dir contains profiles. */
    public File getProfileDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName);
    }

    /**  Deploy directory of AS 5 - "$AS5dir/server/$profile/deploy". */
    public File getDeployDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName, AS5_DEPLOY_DIR);
    }

    /**  Conf directory of AS 5 - "$AS5dir/server/$profile/conf". */
    public File getConfDir() {
        return Utils.createPath(as5dir, AS5_PROFILES_DIR, as5profileName, AS5_CONF_DIR);
    }


    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getDir() {
        return as5dir;
    }

    public void setDir(String as5dir) {
        this.as5dir = as5dir;
    }

    public String getProfileName() {
        return as5profileName;
    }

    public void setProfileName(String profileName) {
        this.as5profileName = profileName;
    }
    //</editor-fold>

}// class
