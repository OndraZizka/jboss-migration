/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils.as7;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Class for building CLI scripts using StringBuilder.
 *
 * @author Roman Jakubco
 */
public class CliAddScriptBuilder {

    private List<String> properties = new LinkedList();

    /**
     * Adds new property to script. Method checks if value isn't empty or null before storing.
     */
    public void addProperty(String property, String value) {
        if( value == null || value.isEmpty() )  return;
        this.properties.add( property + "=" + value);
    }
    
    public void addProperties( Map<String,String> props ){
        for( Map.Entry<String, String> en : props.entrySet() ) {
            addProperty( en.getKey(), en.getValue() );
        }
    }

    /**
     * Formats props like "property=value, property2=value2"
     * and clears them.
     */
    public String formatAndClearProps() {
        String join = StringUtils.join( this.properties, ", " );
        properties.clear();
        return join;
    }

}// class
