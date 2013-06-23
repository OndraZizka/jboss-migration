/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.ex;

import org.jboss.loom.actions.IMigrationAction;

/**
 * @author Roman Jakubco
 */
public class ActionException extends MigrationException {
    
    
    private final IMigrationAction action;
    
    
    public ActionException(IMigrationAction action, String message) {
        super(message);
        this.action = action;
    }

    public ActionException(IMigrationAction action, String message, Throwable cause) {
        super(message, cause);
        this.action = action;
    }

    public ActionException(IMigrationAction action, Throwable cause) {
        super(cause);
        this.action = action;
    }


    public IMigrationAction getAction() {
        return action;
    }
    
    

    public String formatDescription() {
        IMigrationAction action = this.getAction();
        // Header
        String description = 
                  "\n    Migration action which caused the failure: "
                + "  (from " + action.getFromMigrator().getSimpleName() + ")";
        // StackTraceElement
        if( action.getOriginStackTrace() != null )
            description += "\n\tat " + action.getOriginStackTrace().toString();
        // Description
        description += "\n    " + action.toDescription();
        // Origin message
        if( action.getOriginMessage() != null )
            description += "\n    Purpose of the action: " + action.getOriginMessage();
        return description;
    }

    
    
}// class
