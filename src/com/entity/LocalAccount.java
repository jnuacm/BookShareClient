package com.entity;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class LocalAccount extends Application {
	private Account account;
	private List<Book> Books;
	
	public void Initia( ) {
		this.Books = new ArrayList<Book> ();
	}
	
	public void setBooks( List<Book> Books) {
		this.Books = Books;
	}

	public List<Book> getBooks() {
		return this.Books;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return this.account;
	}
}
