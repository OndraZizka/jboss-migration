/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.loom.utils.as7;

/**
 *  Failure of CLI Batch. Contains the index of failed operation and the error message from that operation.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class BatchFailure {
    
    private final Integer index;
    private final String message;


    public BatchFailure( Integer index, String message ) {
        this.index = index;
        this.message = message;
    }
    

    public Integer getIndex() { return index; }
    public String getMessage() { return message; }
    
}
