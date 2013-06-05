/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils.as7;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.loom.ex.MigrationException;

/**
 * Class for building CLI command for CLI API. Use for checking if value is null or empty.
 *
 * @author Roman Jakubco
 */
public class CliApiCommandBuilder {

    private ModelNode command;

    public CliApiCommandBuilder(ModelNode request) {
        this.command = request;
    }

    /**
     * Method for adding new property to ModelNode and checking if its value isn't empty or null
     *
     * @param property name of the property to set
     * @param value    value for setting
     */
    public void addPropertyIfSet(String property, String value) {
        if( value == null || value.isEmpty() )
            return;
        this.command.get(property).set(value);
    }

    public void addPropertyIfSet(String property, String value, String default_) {
        if( value == null || value.isEmpty() )
            value = default_;
        this.command.get(property).set(value);
    }

    public ModelNode getCommand() {
        return command;
    }
    
    
    
    public CliApiCommandBuilder setPropsFromObject(Object obj, String props) throws MigrationException{
        for( String prop : StringUtils.split(props) ){
            try {
                Object val = obj.getClass().getMethod( AS7CliUtils.formatGetterName(prop) ).invoke(obj);
                if( val instanceof Number )
                    val = val.toString();
                if( ! (val instanceof String) )
                    throw new MigrationException("Property " + prop + " is not a string, but " + val.getClass().getName());
                
                this.addPropertyIfSet( prop, (String) val);
            }
            catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                throw new MigrationException("Failed calling getter: " + ex, ex);
            }
        }
        return this;
    }
    
    
}// class
