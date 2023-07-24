package com.retooling.batch.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "farms")
public class Farm {
	
	@Id
	private String farmId;
	private String name;
	private double money;
	private long chickenLimit;
	private long eggLimit;

	public Farm() {
		super();
	}
	
	public Farm(String farmId, String name, double money, long chickenLimit, long eggLimit) {
		super();
		this.farmId = farmId;
		this.name = name;
		this.money = money;
		this.chickenLimit = chickenLimit;
		this.eggLimit = eggLimit;
	}
	
	public String getFarmId() {
		return farmId;
	}
	
	public void setFarmId(String farmId) {
		this.farmId = farmId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public long getChickenLimit() {
		return chickenLimit;
	}

	public void setChickenLimit(long chickenLimit) {
		this.chickenLimit = chickenLimit;
	}

	public long getEggLimit() {
		return eggLimit;
	}

	public void setEggLimit(long eggLimit) {
		this.eggLimit = eggLimit;
	}
	
	@Override
	public String toString() {
		return "Farm [id=" + farmId + ", name=" + name + ", money=" + money + ", chickenLimit=" + chickenLimit
				+ ", eggLimit=" + eggLimit + "]";
	}

}
