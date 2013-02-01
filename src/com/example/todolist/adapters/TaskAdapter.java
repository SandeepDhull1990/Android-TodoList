package com.example.todolist.adapters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todolist.R;
import com.example.todolist.model.Task;

public class TaskAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Task> mTaskList;

	public TaskAdapter(Context context, ArrayList<Task> todoList) {
		this.mContext = context;
		mTaskList = todoList;
	}

	@Override
	public int getCount() {
		return mTaskList.size();
	}

	@Override
	public Object getItem(int position) {
		return mTaskList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_item, null);
		}
		TextView contentView = (TextView) convertView.findViewById(R.id.task_list_item_content);
		TextView completionDateView = (TextView) convertView.findViewById(R.id.task_list_item_completionDate);

		Task task = mTaskList.get(position);
		if (task.getCompletionDate() != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String date = df.format(task.getCompletionDate());
			completionDateView.setText(date);
			contentView.setPaintFlags(contentView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			if ((contentView.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
				contentView.setPaintFlags(contentView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
			}
			completionDateView.setText("");
		}
		contentView.setText(task.getTaskText());
		return convertView;
	}

}
