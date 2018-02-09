package org.celllife.mobilisr.aspect;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.springframework.stereotype.Component;

@Component(value = "simpleBean")
public class SimpleBean {

    private Date dateProperty;

    private Integer integerProperty;

    private String stringProperty;

    @Loggable(LogLevel.TRACE)
    public Date getDateProperty() {
	return dateProperty;
    }

    @Loggable(LogLevel.TRACE)
    public void setDateProperty(final Date dateProperty) {
	this.dateProperty = dateProperty;
    }

    @Loggable(LogLevel.TRACE)
    public Integer getIntegerProperty() {
	return integerProperty;
    }

    @Loggable(LogLevel.TRACE)
    public void setIntegerProperty(final Integer integerProperty) {
	this.integerProperty = integerProperty;
    }

    @Loggable(LogLevel.TRACE)
    public String getStringProperty() {
	return stringProperty;
    }

    @Loggable(LogLevel.TRACE)
    public void setStringProperty(final String stringProperty) {
	this.stringProperty = stringProperty;
    }
    
    @Loggable(LogLevel.TRACE)
    public void multiParamMethod(final String stringProperty, final Integer integerProperty) {
	this.stringProperty = stringProperty;
	this.integerProperty = integerProperty;
    }
    
    @Loggable(LogLevel.TRACE)
    public void throwException() {
	throw new IllegalArgumentException("test exception");
    }

    @Override
    public String toString() {
	return new ToStringBuilder(this).append("dateProperty", dateProperty)
		.append("integerProperty", integerProperty).append("stringProperty", stringProperty).toString();
    }
}
