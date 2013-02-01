package com.example.todolist.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {

	private String mTaskText;
	private Date mCompletionDate;
	private long mTaskId;
	public final static String mSchemaType = "tasks";

	public String getTaskText() {
		return mTaskText;
	}

	public void setTaskText(String mTaskText) {
		this.mTaskText = mTaskText;
	}

	public long getTaskId() {
		return mTaskId;
	}

	public void setTaskId(long mTaskId) {
		this.mTaskId = mTaskId;
	}

	public Date getCompletionDate() {
		return mCompletionDate;
	}
	
	public void setCompletionDate(Date mCompletionDate) {
		this.mCompletionDate = mCompletionDate;
	}
	
	public void setCompletionDate(String mCompletionDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = df.parse(mCompletionDate);
			this.mCompletionDate = date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Task [mTaskText=" + mTaskText + ", mCompletionDate="
				+ mCompletionDate + ", mTaskId=" + mTaskId + "]";
	}
	

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mTaskId);
		dest.writeString(mTaskText);
		if(mCompletionDate != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			dest.writeString(df.format(mCompletionDate));
		}
	}

	public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {

				@Override
				public Task createFromParcel(Parcel source) {
					Task task = new Task();
					task.setTaskId(source.readLong());
					task.setTaskText(source.readString());
					String date = source.readString();
					if(date != null) {
						task.setCompletionDate(date);
					}
					return task;
				}

				@Override
				public Task[] newArray(int size) {
					return null;
				}
	};


}
