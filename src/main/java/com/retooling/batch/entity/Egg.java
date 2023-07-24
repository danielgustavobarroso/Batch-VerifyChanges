package com.retooling.batch.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "eggs")
public class Egg {
	
	@Id
	private String eggId;
	private String farmId;
	private String state;
	private Date creationDate;
	private String origin;
	private Date lastStateChangeDate;

	public Egg() {
		super();
	}
	
	public Egg(String eggId, String farmId, String state, Date creationDate, String origin, Date lastStateChangeDate) {
		super();
		this.eggId = eggId;
		this.farmId = farmId;
		this.state = state;
		this.creationDate = creationDate;
		this.origin = origin;
		this.lastStateChangeDate = lastStateChangeDate;
	}

	public String getEggId() {
		return eggId;
	}
	public void setEggId(String eggId) {
		this.eggId = eggId;
	}
	
	public String getFarmId() {
		return farmId;
	}

	public void setFarmId(String farmId) {
		this.farmId = farmId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Date getLastStateChangeDate() {
		return lastStateChangeDate;
	}

	public void setLastStateChangeDate(Date lastStateChangeDate) {
		this.lastStateChangeDate = lastStateChangeDate;
	}

	@Override
	public String toString() {
		return "Egg [eggId=" + eggId + ", farmId=" + farmId + ", state=" + state + ", creationDate=" + creationDate
				+ ", origin=" + origin + ", lastStateChangeDate=" + lastStateChangeDate + "]";
	}
	
}
