/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.ex;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RollbackMigrationException extends MigrationException {

    private Throwable rollbackCause;


    /**
     * For wrapping both rollback cause and exception during rollback.
     * Maybe we could get rid of the other constructors.
     */
    public RollbackMigrationException(Throwable rollbackCause, Throwable originalCause) {
        super(rollbackCause);
    }

    public RollbackMigrationException(String message) {
        super(message);
    }

    public RollbackMigrationException(String message, Throwable cause) {
        super(message, cause);
    }


    public RollbackMigrationException(Throwable cause) {
        super(cause);
    }


    public Throwable getRollbackCause() {
        return rollbackCause;
    }

    public void setRollbackCause(Throwable rollbackCause) {
        this.rollbackCause = rollbackCause;
    }

}// class
