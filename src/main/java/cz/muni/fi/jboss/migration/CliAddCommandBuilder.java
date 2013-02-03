package cz.muni.fi.jboss.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class for building Cli scripts using StringBuilder.
 *
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 2:38 PM
 */
public class CliAddCommandBuilder {

    private List<String> properties = new ArrayList();

    /**
     * Method for adding new property to script. Method checks if value isn't empty or null before storing.
     *
     * @param property which should be set
     * @param value value of property
     */
    public void addProperty(String property, String value){
        if(value != null){
            if (!value.isEmpty()) {
                this.properties.add(property + "=" +value);
            }
        }
    }

    /**
     * Method for writing stored data to string in predefine format(property=value, property2=value2).
     *
     * @return string containing all data
     */
    public String asString(){
       StringBuilder builder = new StringBuilder();

       for(int i = 0; i < this.properties.size(); i++){
           if(i == this.properties.size()-1){
              builder.append(this.properties.get(i));
               continue;
           }
           builder.append(this.properties.get(i));
           builder.append(", ");
       }

       properties.clear();

       return builder.toString();
    }


}
