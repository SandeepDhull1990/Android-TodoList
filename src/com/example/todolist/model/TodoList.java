package com.example.todolist.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class TodoList implements Parcelable{

	private long mObjectId;
	private String mListTitle;
	private Date mCompletionDate;
	
	public void setListTitle(String listTitle) {
		this.mListTitle = listTitle;
	}
	
	public String getListTitle() {
		return this.mListTitle;
	}

	public void setObjectId(long objectId) {
		this.mObjectId = objectId;
	}
	
	public long getObjectId() {
		return this.mObjectId;
	}

	public void setCompletionDate(String date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		try {
			this.mCompletionDate = df.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Date getCompletionDate() {
		return mCompletionDate;
	}

	public void setCompletionDate(Date mCompletedDate) {
		this.mCompletionDate = mCompletedDate;
	}
	
	@Override
	public String toString() {
		return "TodoList [mObjectId=" + mObjectId + ", mListTitle="
				+ mListTitle + ", mCompletionDate=" + mCompletionDate + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mObjectId);
		dest.writeString(mListTitle);
	}

	public static final Parcelable.Creator<TodoList> CREATOR = new Parcelable.Creator<TodoList>() {

				@Override
				public TodoList createFromParcel(Parcel source) {
					TodoList todos = new TodoList();
					todos.setObjectId(source.readLong());
					todos.setListTitle(source.readString());
					return todos;
				}

				@Override
				public TodoList[] newArray(int size) {
					return null;
				}
	};
	
}
