package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.pconfig.model.Pconfig;

public class MessageFilterViewModel implements MobilisrEntity, Serializable {

	private static final long serialVersionUID = -6601514470778342782L;

	public static final String PROP_TYPE_DESCRIPTOR = "typeDescriptor";

	private MessageFilter messageFilter;

	private Pconfig typeDescriptor;

	private List<Pconfig> actionDescriptors = new ArrayList<Pconfig>();

	private String name;

	private Channel channel;

	private Organization organization;

	public MessageFilterViewModel() {}

	public MessageFilterViewModel(MessageFilter messageFilter) {
		this.messageFilter = messageFilter;
		init(messageFilter);
	}

	private void init(MessageFilter messageFilter) {
		this.name = messageFilter.getName();
		this.organization = messageFilter.getOrganization();
		this.channel = messageFilter.getChannel();
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setChannel(Channel channel){
		this.channel = channel;
	}

	public Channel getChannel(){
		return channel;
	}

	public void setOrganization(Organization organization){
		this.organization = organization;
	}

	public Organization getOrganization(){
		return organization;
	}

	public MessageFilter getMessageFilter() {
		if (messageFilter != null){
			messageFilter.setName(name);
			messageFilter.setOrganization(organization);
			messageFilter.setChannel(channel);
			
			messageFilter.setType(typeDescriptor.getResource());
		}
		return messageFilter;
	}

	public void setMessageFilter(MessageFilter messageFilter) {
		this.messageFilter = messageFilter;
		init(messageFilter);
	}

	public List<Pconfig> getActionDescriptors() {
		return actionDescriptors;
	}

	public void setActionDescriptors(List<Pconfig> actionDescriptors) {
		this.actionDescriptors = actionDescriptors;
	}

	public void addActionDescriptor(Pconfig descriptor){
		this.actionDescriptors.add(descriptor);
	}

	public Pconfig getTypeDescriptor() {
		return typeDescriptor;
	}

	public void setTypeDescriptor(Pconfig typeDescriptor) {
		this.typeDescriptor = typeDescriptor;
	}
	
	@Override
	public void setId(Long id) {
	}
	
	@Override
	public Long getId() {
		return messageFilter.getId();
	}
	@Override
	public String getIdentifierString() {
		return messageFilter.getIdentifierString();
	}
	
	@Override
	public boolean isPersisted() {
		return messageFilter.isPersisted();
	}
}
