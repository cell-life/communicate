package org.celllife.mobilisr.api.validation;

import org.celllife.mobilisr.constants.ErrorCode;

/**
 * This class previously formed part of MsisdnValidator, but was stripped out in order to make it generic.
 */
public class ValidationError {

        private ErrorCode code;
        private String message;

        public ValidationError(ErrorCode code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ErrorCode  getCode() {
            return code;
        }

        public void setCode(ErrorCode  code) {
            this.code = code;
        }
    }
