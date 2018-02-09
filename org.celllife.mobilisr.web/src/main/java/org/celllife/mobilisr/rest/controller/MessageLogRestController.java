package org.celllife.mobilisr.rest.controller;

import org.celllife.mobilisr.api.rest.MessageLogDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.MessageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Controller("messageLogRestController")
@RequestMapping("/v2/messageLog")
public class MessageLogRestController {

    protected ApiVersion apiVersion;

    @Autowired
    private MessageLogService messageLogService;

    public MessageLogRestController() {
        setApiVersion(ApiVersion.getLatest());
    }

    public void setApiVersion(ApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    @ResponseBody
    public MessageLogDto getMessageLog(HttpServletResponse response, @PathVariable("id") final Long messageId) {

        SmsLog smsLog = messageLogService.getMessageLog(messageId); //FIXME: nullpointer exception on this line
        return convertToDto(smsLog);

    }

    public MessageLogDto convertToDto(SmsLog smsLog) {

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        MessageLogDto messageLogDto = new MessageLogDto();
        messageLogDto.setId(smsLog.getId());
        messageLogDto.setMsisdn(smsLog.getMsisdn());
        messageLogDto.setStatus(smsLog.getStatus());
        messageLogDto.setFailreason(smsLog.getFailreason());
        messageLogDto.setDateTime(df.format(smsLog.getDatetime()));

        return messageLogDto;
    }

}
