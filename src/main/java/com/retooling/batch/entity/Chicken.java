package com.retooling.batch.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chickens")
public class Chicken {
	
	@Id
	private String chickenId;
	private String farmId;
	private String state;
	private Date creationDate;
	private String origin;
	private Date lastEggDate;
	private Date lastStateChangeDate;

	public Chicken() {
		super();
	}
	
	public Chicken(String chickenId, String farmId, String state, Date creationDate, String origin,
			Date lastEggDate, Date lastStateChangeDate) {
		super();
		this.chickenId = chickenId;
		this.farmId = farmId;
		this.state = state;
		this.creationDate = creationDate;
		this.origin = origin;
		this.lastEggDate = lastEggDate;
		this.lastStateChangeDate = lastStateChangeDate;
	}
	
	public String getChickenId() {
		return chickenId;
	}

	public void setChickenId(String chickenId) {
		this.chickenId = chickenId;
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

	public Date getLastEggDate() {
		return lastEggDate;
	}

	public void setLastEggDate(Date lastEggDate) {
		this.lastEggDate = lastEggDate;
	}

	public Date getLastStateChangeDate() {
		return lastStateChangeDate;
	}

	public void setLastStateChangeDate(Date lastStateChangeDate) {
		this.lastStateChangeDate = lastStateChangeDate;
	}

	@Override
	public String toString() {
		return "Chicken [chickenId=" + chickenId + ", farmId=" + farmId + ", state=" + state + ", creationDate="
				+ creationDate + ", origin=" + origin + ", lastEggDate=" + lastEggDate + ", lastStateChangeDate="
				+ lastStateChangeDate + "]";
	}
	
}
