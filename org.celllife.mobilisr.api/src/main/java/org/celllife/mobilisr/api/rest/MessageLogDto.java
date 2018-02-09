package org.celllife.mobilisr.api.rest;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.SmsStatus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="messagelog")
@XmlType(name="MessageLog", propOrder = {"id","msisdn","status","failreason","dateTime"})
public class MessageLogDto implements MobilisrDto {

    private static final long serialVersionUID = 1069873454296703290L;

    private Long id;

    private String msisdn;

    private SmsStatus status;

    private String failreason;

    private String dateTime;

    public Long getId() {
        return id;
    }

    @XmlElement(name="id", required=true)
    public void setId(Long id) {
        this.id = id;
    }

    public String getMsisdn() {
        return msisdn;
    }

    @XmlElement(name="msisdn", required=true)
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public SmsStatus getStatus() {
        return status;
    }

    @XmlElement(name="status", required=true)
    public void setStatus(SmsStatus status) {
        this.status = status;
    }

    public String getFailreason() {
        return failreason;
    }

    @XmlElement(name="failreason", required=false)
    public void setFailreason(String failreason) {
        this.failreason = failreason;
    }

    public String getDateTime() {
        return dateTime;
    }

    @XmlElement(name="dateTime", required=true)
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

}
