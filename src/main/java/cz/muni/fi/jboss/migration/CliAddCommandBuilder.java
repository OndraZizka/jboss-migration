package cz.muni.fi.jboss.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Jakubco
 *         Date: 2/2/13
 *         Time: 2:38 PM
 */
public class CliAddCommandBuilder {
    private List<String> properties = new ArrayList();

    public void addProperty(String property, String value){
        if(value != null){
            if (!value.isEmpty()) {
                this.properties.add(property + "=" +value);
            }
        }
    }

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
       return builder.toString();
    }

    public void clear(){
        this.properties.clear();
    }
}
