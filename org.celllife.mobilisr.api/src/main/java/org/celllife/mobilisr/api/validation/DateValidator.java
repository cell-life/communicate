package org.celllife.mobilisr.api.validation;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.celllife.mobilisr.constants.ErrorCode;

import java.text.ParseException;
import java.util.Date;

public class DateValidator {

    /**
     * This will return a validation error, if any exists. Otherwise it will return null.
     * @param dateString String to validate.
     * @return Validation error if any, otherwise 'null'.
     */
    public ValidationError validate(String dateString) {

        Date date;

        try {
            DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
            date = fmt.parse(dateString);
        } catch (IllegalArgumentException e) {
            return new ValidationError(ErrorCode.INVALID_DATE_FORMAT, e.getLocalizedMessage().toString());
        }

        return null;

    }

}
