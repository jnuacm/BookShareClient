package com.socket;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.entity.Book;
import com.entity.Comment;
public class AQuery extends Ackownledge{
	
	public AQuery(){
		super(true);
	}
	
	public static List<Book> receiveBooklist(){
		List<Book> booklist = new ArrayList<Book>();
		try {
			JSONArray jsonArray = Communication.receive().getJSONArray("booklist");
		//	JSONArray jsonArray = new JSONArray(Communication.receive().toString());
			for(int i = 0;i < jsonArray.length();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				booklist.add(new Book(jsonObject));
			}
			return booklist;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Comment> receiveCommentlist(){
		List<Comment> commentlist = new ArrayList<Comment>();
		try {
			JSONArray jsonArray = Communication.receive().getJSONArray("commentlist");
			for(int i = 0;i < jsonArray.length();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				commentlist.add(new Comment(jsonObject));
			}
			return commentlist;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
