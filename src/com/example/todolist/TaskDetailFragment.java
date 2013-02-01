package com.example.todolist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.callbacks.AppacitiveFetchCallback;
import com.appacitive.android.model.AppacitiveConnection;
import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveObject;
import com.example.todolist.adapters.TaskAdapter;
import com.example.todolist.model.Task;

/**
 * A fragment representing a single Task detail screen. This fragment is either
 * contained in a {@link TaskListActivity} in two-pane mode (on tablets) or a
 * {@link TaskDetailActivity} on handsets.
 */
public class TaskDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	public static final String TASK_LIST = "task_list";

	private static final int TASK_DETAIL_GROUP_ID= 101;
	private static final int TASK_DELETE = 3;
	private static final int TASK_CLOSE = 4;

	private View mLoadingView;
	private ListView mTaskListView;
	private Button mAddTaskButton;
	private EditText mAddTaskEditText;

	private ArrayList<Task> mTaskList;
	private TaskAdapter mAdapter;
	
	private long mListId;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mListId = getArguments().getLong(ARG_ITEM_ID);
		}
		if(savedInstanceState != null) {
			mTaskList = savedInstanceState.getParcelableArrayList(TASK_LIST);
		}
		if(mTaskList == null) {
			mTaskList = new ArrayList<Task>();
		}
		mAdapter = new TaskAdapter(getActivity(), mTaskList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_task, container,
				false);

		mTaskListView = (ListView) rootView.findViewById(R.id.fragment_task_listView);
		mAddTaskEditText = (EditText) rootView.findViewById(R.id.fragment_task_task_list_editText);
		mAddTaskButton = (Button) rootView.findViewById(R.id.fragment_task_task_list_addButton);
		
		mLoadingView = rootView.findViewById(R.id.fragment_task_status);
		
		mAddTaskButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String taskTitle = mAddTaskEditText.getText().toString();
				if(!taskTitle.equals("")) {
					saveTask(mListId, taskTitle);
				}
				mAddTaskEditText.setText("");
			}
		});
		
		mTaskListView.setAdapter(mAdapter);
		
		registerForContextMenu(mTaskListView);

		if(mTaskList.size() == 0) {
			fetchTasks(mListId);
		}
		return rootView;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(TASK_DETAIL_GROUP_ID, TASK_DELETE, 0, "Delete");
		menu.add(TASK_DETAIL_GROUP_ID, TASK_CLOSE, 0, "Close");
		menu.setHeaderTitle("Options");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(TASK_LIST, mTaskList);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case TASK_DELETE:
			Task task = mTaskList.get(info.position);
			deleteTask(task);
			break;
		case TASK_CLOSE:
			task = mTaskList.get(info.position);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String date = df.format(new Date());
			closeTask(task, date);
			break;
		}
		return true;
	}

	private void fetchTasks(final long objectId) {
		mLoadingView.setVisibility(View.VISIBLE);
		AppacitiveConnection.searchForConnectedArticles("list_items", objectId,
				new AppacitiveFetchCallback() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(Map<String, Object> response) {
						List<Object> connections = (List<Object>) response
								.get("connections");
						if (connections != null) {
							updateTaskList(connections);
						}
					}

					@Override
					public void onFailure(AppacitiveError error) {
						Log.d("TAG", "Error is " + error.toString());
						if (TaskDetailFragment.this.isResumed()) {
							getActivity().runOnUiThread(new Runnable() {
	
								@Override
								public void run() {
									mLoadingView.setVisibility(View.GONE);
								}
							});
						}
					}

					@SuppressWarnings("unchecked")
					private void updateTaskList(final List<Object> connections) {

						for (int i = 0; i < connections.size(); i++) {
							Map<String, Object> connection = (Map<String, Object>) connections
									.get(i);
							Map<String, Object> article = (Map<String, Object>) ((Map<String, Object>) connection
									.get("__endpointb")).get("article");
							String taskText = (String) article.get("text");
							long id = Long.parseLong((String) article.get("__id"));
							String completionDate = (String)article.get("completed_at");
							Task todoTask = new Task();
							todoTask.setTaskText(taskText);
							if(completionDate != null) {
								todoTask.setCompletionDate(completionDate);
							}
							todoTask.setTaskId(id);
							mTaskList.add(todoTask);
						}
						if (TaskDetailFragment.this.isResumed()) {
							getActivity().runOnUiThread(new Runnable() {
	
								@Override
								public void run() {
									mLoadingView.setVisibility(View.GONE);
									mAdapter.notifyDataSetChanged();
								}
							});
						}
					}
				});

	}

	private void saveTask(final long todoListId, final String taskTitle) {
		final Task task = new Task();
		task.setTaskText(taskTitle);
		mTaskList.add(0, task);
		mAdapter.notifyDataSetChanged();
		
		final AppacitiveObject object = new AppacitiveObject("tasks");
		object.addProperty("text", taskTitle);
		object.saveObject(new AppacitiveCallback() {

			@Override
			public void onSuccess() {
				AppacitiveConnection connection = new AppacitiveConnection("list_items");
				connection.setArticleAId(todoListId);
				connection.setLabelA("todolists");
				connection.setArticleBId(object.getObjectId());
				connection.setLabelB("tasks");

				connection.createConnection(new AppacitiveCallback() {

					@Override
					public void onSuccess() {
						task.setTaskId(object.getObjectId());
					}

					@Override
					public void onFailure(AppacitiveError error) {
						Log.d("TAG", "Error is " + error.toString());
					}
				});

			}

			@Override
			public void onFailure(AppacitiveError error) {
				Log.d("TAG", "Error is " + error.toString());
			}
		});
	}

	private void closeTask(final Task task, String date) {
		task.setCompletionDate(date);
		mAdapter.notifyDataSetChanged();
		Log.d("TAG", "The task is " + task.toString());
		AppacitiveObject object = new AppacitiveObject("tasks");
		object.setObjectId(task.getTaskId());
		object.addProperty("completed_at", date);
		object.updateObject(new AppacitiveCallback() {

			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure(AppacitiveError error) {
				Log.d("TAG", "Error is " + error.toString());
			}
		});
	}
	
	private void deleteTask(final Task task) {
		mTaskList.remove(task);
		mAdapter.notifyDataSetChanged();
		
		AppacitiveObject object = new AppacitiveObject("tasks");
		object.setObjectId(task.getTaskId());
		object.deleteObjectWithConnections(true, new AppacitiveCallback() {

			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure(AppacitiveError error) {
				Log.d("TAG", "Error is " + error.toString());
			}
		});
	}

	
}
