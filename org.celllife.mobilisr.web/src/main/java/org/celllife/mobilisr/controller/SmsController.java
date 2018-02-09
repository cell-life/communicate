package org.celllife.mobilisr.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.celllife.mobilisr.api.messaging.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller("smsController")
public class SmsController {
	
	private static Logger log = LoggerFactory.getLogger(SmsController.class);
	
	@Autowired
	private ApplicationContext context;
	
	@RequestMapping(value="/incoming/{channel}", method={RequestMethod.GET})
	@ResponseStatus(value=HttpStatus.OK)
	public void incomingGet(HttpServletRequest request, @PathVariable("channel") String handler) {
		deliverMessageToChannel(request, "in-" + handler, null);
	}
	
	@RequestMapping(value="/incoming/{channel}", method={RequestMethod.POST})
	@ResponseStatus(value=HttpStatus.OK)
	public void incomingPost(HttpServletRequest request, @PathVariable("channel") String handler,
			@RequestBody String body) {
		deliverMessageToChannel(request, "in-" + handler, body);
	}
	
	@RequestMapping(value="/callback/{channel}", method={RequestMethod.GET})
	@ResponseStatus(value=HttpStatus.OK)
	public void waspCallbackGet(HttpServletRequest request, @PathVariable("channel") String handler){
		deliverMessageToChannel(request, "delivery-" + handler, null);
	}

	@RequestMapping(value="/callback/{channel}", method={RequestMethod.POST})
	@ResponseStatus(value=HttpStatus.OK)
	public void waspCallbackPost(HttpServletRequest request, @PathVariable("channel") String handler,
			@RequestBody String body){
		deliverMessageToChannel(request, "delivery-" + handler, body);
	}

	private void deliverMessageToChannel(HttpServletRequest request,
			String handler, String body) {
		MessageChannel channel = context.getBean(handler, MessageChannel.class);
		
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = request.getParameterMap();
		
		if (channel == null){
			log.warn("Incoming message on unknown channel [{}], [body={}], [params={}]", 
					new Object[]{handler, body, parameterMap});
			return;
		}
		
		RawMessage rawMessage = new RawMessage(body, parameterMap);
		Message<RawMessage> message = new GenericMessage<RawMessage>(rawMessage);
		channel.send(message);
	}
}
