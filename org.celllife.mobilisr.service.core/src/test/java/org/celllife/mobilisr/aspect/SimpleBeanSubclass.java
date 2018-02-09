package org.celllife.mobilisr.aspect;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.springframework.stereotype.Component;

@Component(value = "simpleBeanSubclass")
public class SimpleBeanSubclass extends SimpleBean {

    private BigDecimal decimalProperty;

    @Loggable(LogLevel.TRACE)
    public BigDecimal getDecimalProperty() {
	return decimalProperty;
    }

    @Loggable(LogLevel.TRACE)
    public void setDecimalProperty(final BigDecimal decimalProperty) {
	this.decimalProperty = decimalProperty;
    }

    @Override
    public String toString() {
	return new ToStringBuilder(this).append("decimalProperty", decimalProperty).appendSuper(super.toString())
		.toString();
    }
}
