package org.celllife.mobilisr.api.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.celllife.mobilisr.api.MobilisrDto;



@XmlRootElement(name = "list")
@XmlType(name="PagedList")
public class PagedListDto<T extends MobilisrDto> implements MobilisrDto {

	private static final long serialVersionUID = 6279924053853282621L;
	
	private Integer offset;
	private Integer limit;
	private Integer total;
	private List<T> elements;

	public PagedListDto() {
	}
	
	public PagedListDto(Integer offset, Integer limit, Integer total) {
		this.offset = offset;
		this.limit = limit;
		this.total = total;
	}

	public PagedListDto(List<T> elements) {
		addElements(elements);
	}
	
	public boolean addElements(List<T> elements){
		if (this.elements == null){
			this.elements = new ArrayList<T>();
		}
		return this.elements.addAll(elements);
	}

	public boolean addElement(T element){
		if (this.elements == null){
			this.elements = new ArrayList<T>();
		}
		return this.elements.add(element);
	}

	public List<T> getElements() {
		return elements;
	}

	/**
	 * @return The number of records being requested.
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @return The offset for the first record to retrieve.
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * @return The total number of objects available.
	 */
	public Integer getTotal() {
		return total;
	}

	@XmlElements({ @XmlElement(name = "campaign", type = CampaignDto.class),
		@XmlElement(name = "contact", type = ContactDto.class),
		@XmlElement(name = "err", type = ErrorDto.class),
		@XmlElement(name = "messageStatus", type = MessageStatusDto.class),
		@XmlElement(name = "message", type = MessageDto.class) })
	public void setElements(List<T> elements) {
		this.elements = elements;
	}

	@XmlAttribute(name = "limit", required = false)
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@XmlAttribute(name = "offset", required = false)
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	@XmlAttribute(name = "total", required = false)
	public void setTotal(Integer total) {
		this.total = total;
	}
	
	public int size() {
		return elements.size();
	}
	
	public Class<? extends MobilisrDto> getType(){
		if (elements != null && !elements.isEmpty()){
			return elements.get(0).getClass();
		}
		return null;
	}

	@Override
	public String toString() {
		return "PagedListDto [offset=" + offset + ", limit=" + limit
				+ ", total=" + total + ", elements=" + elements + "]";
	}

	public boolean isEmpty() {
		return elements == null || elements.isEmpty();
	}
	
}
