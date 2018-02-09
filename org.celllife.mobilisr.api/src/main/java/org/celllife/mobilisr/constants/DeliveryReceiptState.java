/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.celllife.mobilisr.constants;

public enum DeliveryReceiptState {
    /**
     * DELIVERED
     */
    DELIVRD(1,"Message has been delivered"),
    /**
     * EXPIRED
     */
    EXPIRED(2,"Message validity period has expired"),
    /**
     * DELETED
     */
    DELETED(3,"Message has been deleted"),
    /**
     * UNDELIVERABLE
     */
    UNDELIV(4,"Message could not be delivered"),
    /**
     * ACCEPTED
     */
    ACCEPTD(5,"Message is in accepted state"),
    /**
     * UNKNOWN
     */
    UNKNOWN(6,"Message is in unknown state"),
    /**
     * REJECTED
     */
    REJECTD(7,"Message has been rejected");

    private int value;
    private String message;

    private DeliveryReceiptState(int value, String message) {
        this.value = value;
		this.message = message;
    }

    public static DeliveryReceiptState getByName(String name) {
        return valueOf(DeliveryReceiptState.class, name);
    }

    public static DeliveryReceiptState valueOf(int value)
            throws IllegalArgumentException {
        for (DeliveryReceiptState item : values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalArgumentException(
                "No enum const DeliveryReceiptState with value " + value);
    }

    public int value() {
        return value;
    }
    
    public String getMessage() {
		return message;
	}
}
