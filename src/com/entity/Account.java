package com.entity;

public class Account {
	private String library,account,password,name;
	public Account(String library, String account, String password, String name){
		this.library = library;
		this.account = account;
		this.password = password;
		this.name = name;
	}
	public String getLibrary(){
		return library;
	}
	public String getAccount(){
		return account;
	}
	public String getPassword(){
		return password;
	}
	public String getName(){
		return name;
	}
}
