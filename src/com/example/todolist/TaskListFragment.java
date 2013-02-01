package com.example.todolist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.callbacks.AppacitiveFetchCallback;
import com.appacitive.android.model.AppacitiveConnection;
import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveObject;
import com.appacitive.android.model.AppacitiveUser;
import com.example.todolist.adapters.TodoListAdapter;
import com.example.todolist.model.TodoList;

/**
 * A list fragment representing a list of TodoList. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link TaskDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class TaskListFragment extends Fragment implements OnItemClickListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private static final String TODO_LIST = "todo_list";
	private static final int TODO_LIST_GROUPID = 102;
	private static final int TODO_LIST_CLOSE = 1;
	private static final int TODO_LIST_DELETE = 2;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(long id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(long id) {
		}
	};
	
	private View mLoadingView;
	private ListView mTodoListView;
	private Button mAddTodoListButton;
	private EditText mAddTodoListEditText;

	private ArrayList<TodoList> mTodoList;
	private TodoListAdapter mAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public TaskListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mTodoList = savedInstanceState.getParcelableArrayList(TODO_LIST);
		}
		if (mTodoList == null) {
			mTodoList = new ArrayList<TodoList>();
		}
		mAdapter = new TodoListAdapter(getActivity(), mTodoList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_todo, null);
		mTodoListView = (ListView) v.findViewById(R.id.fragment_todo_listView);
		mAddTodoListButton = (Button) v.findViewById(R.id.fragment_todo_todo_list_addButton);
		mAddTodoListEditText = (EditText) v.findViewById(R.id.fragment_todo_todo_list_editText);

		mLoadingView = v.findViewById(R.id.fragment_todo_status);
		mTodoListView.setOnItemClickListener(this);
		mTodoListView.setAdapter(mAdapter);

		mAddTodoListButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String todoListTitle = mAddTodoListEditText.getText().toString().trim();
				if(!todoListTitle.equals("")) {
					saveTodoList(todoListTitle);
				}
				mAddTodoListEditText.setText("");
			}
		});

		registerForContextMenu(mTodoListView);
		
		if(mTodoList.size() == 0) {
			fetchTodoList();
		}
		
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		outState.putParcelableArrayList(TODO_LIST, mTodoList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(TODO_LIST_GROUPID, TODO_LIST_DELETE, 0, "Delete List");
		menu.add(TODO_LIST_GROUPID, TODO_LIST_CLOSE, 0, "Close List");
		menu.setHeaderTitle("Options");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getGroupId() == TODO_LIST_GROUPID) {
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			switch (item.getItemId()) {
			case TODO_LIST_DELETE:
				TodoList todoList = mTodoList.get(info.position);
				deleteTodoList(todoList);
				break;
			case TODO_LIST_CLOSE:
				todoList = mTodoList.get(info.position);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String date = df.format(new Date());
				closeTodoList(todoList, date);
				break;
			}
			return true;
		}
		return false;
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		mTodoListView.setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			mTodoListView.setItemChecked(mActivatedPosition, false);
		} else {
			mTodoListView.setItemChecked(position, true);
		}
		mActivatedPosition = position;
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View row, int position,
			long id) {
		setActivatedPosition(position);
		mCallbacks.onItemSelected(mTodoList.get(position).getObjectId());
	}

	private void fetchTodoList() {
		mLoadingView.setVisibility(View.VISIBLE);
		AppacitiveConnection.searchForConnectedArticles("user_lists",
				AppacitiveUser.currentUser.getObjectId(),
				new AppacitiveFetchCallback() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(Map<String, Object> response) {
						List<Object> connections = (List<Object>) response.get("connections");
						if (connections != null) {
							createTodoList(connections);
						}
					}
					
					@Override
					public void onFailure(AppacitiveError error) {
						Log.d("TAG", "Error : " + error.toString() );
						if (TaskListFragment.this.isResumed()) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (mTodoListView != null) {
										mLoadingView.setVisibility(View.GONE);
									}
								}
							});
						}

					}

					@SuppressWarnings("unchecked")
					private void createTodoList(final List<Object> connections) {
						for (int i = 0; i < connections.size(); i++) {
							Map<String, Object> connection = (Map<String, Object>) connections
									.get(i);
							Map<String, Object> article = (Map<String, Object>) ((Map<String, Object>) connection
									.get("__endpointb")).get("article");
							String listTitle = (String) article.get("list_name");
							String date = (String) article.get("completed_at");
							long id = Long.parseLong((String) article.get("__id"));
							TodoList todoList = new TodoList();
							todoList.setListTitle(listTitle);
							if(date != null) {
								todoList.setCompletionDate(date);
							}
							todoList.setObjectId(id);
							mTodoList.add(todoList);
						}
						
						if (TaskListFragment.this.isResumed()) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (mTodoListView != null) {
										mLoadingView.setVisibility(View.GONE);
										mAdapter.notifyDataSetChanged();
									}
								}
							});
						}
					}
				});
	}

	private void saveTodoList(final String taskListTitle) {
		final TodoList taskList = new TodoList();
		taskList.setListTitle(taskListTitle);
		mTodoList.add(0, taskList);
		mAdapter.notifyDataSetChanged();
		final AppacitiveObject object = new AppacitiveObject("todolists");
		object.addProperty("list_name", taskListTitle);
		object.saveObject(new AppacitiveCallback() {

			@Override
			public void onSuccess() {

				AppacitiveConnection connection = new AppacitiveConnection(
						"user_lists");
				connection.setArticleAId(AppacitiveUser.currentUser
						.getObjectId());
				connection.setLabelA("user");

				connection.setArticleBId(object.getObjectId());
				connection.setLabelB("todolists");

				connection.createConnection(new AppacitiveCallback() {

					@Override
					public void onSuccess() {
						taskList.setObjectId(object.getObjectId());
					}

					@Override
					public void onFailure(AppacitiveError error) {
						Log.w("TAG", "Error Saving the list");
					}
				});

			}

			@Override
			public void onFailure(AppacitiveError error) {
				Log.w("TAG", "Error Saving the list");
			}
		});
	}

	private void deleteTodoList(final TodoList list) {
		mTodoList.remove(list);
		mAdapter.notifyDataSetChanged();

		AppacitiveObject object = new AppacitiveObject("todolists");
		object.setObjectId(list.getObjectId());
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

	private void closeTodoList(final TodoList todoList, String date) {
		
		todoList.setCompletionDate(date);
		mAdapter.notifyDataSetChanged();
		AppacitiveObject object = new AppacitiveObject("todolists");
		object.setObjectId(todoList.getObjectId());
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

}
