package com.test.netty4.model;

public class Person {  
    private int id;  
    private String name;
    private String currDate;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Person() {
		super();
	}
	
	public Person(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public String getCurrDate() {
		return currDate;
	}
	public void setCurrDate(String currDate) {
		this.currDate = currDate;
	}
	
	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", currDate=" + currDate + "]";
	}
    
}