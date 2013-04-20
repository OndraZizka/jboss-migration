package cz.muni.fi.jboss.migration;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Class for building Cli scripts using StringBuilder.
 *
 * @author Roman Jakubco
 */
public class CliAddScriptBuilder {

    private List<String> properties = new LinkedList();

    /**
     * Method for adding new property to script. Method checks if value isn't empty or null before storing.
     *
     * @param property which should be set
     * @param value    value of property
     */
    public void addProperty(String property, String value) {
        if( value == null || value.isEmpty() )  return;
        this.properties.add( property + "=" + value);
    }

    /**
     * Method for writing stored data to string in predefine format(property=value, property2=value2).
     *
     * @return string containing all data
     */
    public String asString() {

        /*StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.properties.size(); i++) {
            builder.append(this.properties.get(i));
            if (i < this.properties.size() - 1)
                builder.append(", ");
        }
        properties.clear();
        return builder.toString();*/
        
        String join = StringUtils.join( this.properties, ", " );
        properties.clear();
        return join;
    }

    /**
     * Method for writing stored data to string in predefine format(property=value property2=value2).
     *
     * @return string containing all data
     */
    public String asStringNew() {
        /*StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.properties.size(); i++) {
        if (i == this.properties.size() - 1) {
        builder.append(this.properties.get(i));
        continue;
        }
        builder.append(this.properties.get(i));
        builder.append(" ");
        }
        properties.clear();
        return builder.toString();*/
        
        String join = StringUtils.join( this.properties, " " );
        properties.clear();
        return join;
    }

}// class
