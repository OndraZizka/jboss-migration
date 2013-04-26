package org.jboss.loom;

import org.jboss.dmr.ModelNode;

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
    public void addProperty(String property, String value) {
        if( value == null || value.isEmpty() )
            return;
        this.command.get(property).set(value);
    }

    public ModelNode getCommand() {
        return command;
    }
}
