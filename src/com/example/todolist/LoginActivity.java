package com.example.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.appacitive.android.callbacks.AppacitiveAuthenticationCallback;
import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.model.Appacitive;
import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveUser;
import com.example.todolist.model.Constants;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mUserNameView;
	private EditText mPasswordView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

//	Handler to ui thread
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		mHandler = new Handler();

		if (Appacitive.getInstance() == null) {
			Appacitive.initializeAppacitive(getApplicationContext(),
					Constants.API_KEY, new AppacitiveCallback() {

						@Override
						public void onSuccess() {
							Log.d("TAG", "Appacitive object is initialized");
						}

						@Override
						public void onFailure(AppacitiveError error) {
							AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
							alertDialog.setTitle("Error...");
							alertDialog.setMessage(error.getMessage());
							alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
							alertDialog.setNegativeButton("Exit",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,int which) {
											Appacitive.endSession();
											finish();
											dialog.cancel();
										}
									});
							alertDialog.show();
						}
					});
		}

		mUserNameView = (EditText) findViewById(R.id.userName);
		mUserNameView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						view.setEnabled(false);
						findViewById(R.id.register_button).setEnabled(false);
						attemptLogin();
					}
				});
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(LoginActivity.this,
								SignUpActivity.class);
						startActivity(intent);
					}
				});

	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// Reset errors.
		mUserNameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mUserNameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError("Password can't be empty");
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 3) {
			mPasswordView.setError("Wrong Password");
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mUserNameView.setError("Username can't be null");
			focusView = mUserNameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText("Signing in..");
			mLoginStatusView.bringToFront();
			mLoginStatusView.setVisibility(View.VISIBLE);
			String userName = mUserNameView.getText().toString();
			String password = mPasswordView.getText().toString();
			signIn(userName, password);
		}
	}

	private void signIn(String userName, String password) {
		AppacitiveUser.authenticate(userName, password,
				new AppacitiveAuthenticationCallback() {

					@Override
					public void onSuccess() {
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								mLoginStatusMessageView.setText("Login Successfull !");
								mLoginStatusView.setVisibility(View.GONE);
								findViewById(R.id.sign_in_button).setEnabled(true);
								findViewById(R.id.register_button).setEnabled(true);
								Intent intent = new Intent(LoginActivity.this,TaskListActivity.class);
								startActivity(intent);
							}
						});
					}

					@Override
					public void onFailure(AppacitiveError error) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								findViewById(R.id.sign_in_button).setEnabled(true);
								findViewById(R.id.register_button).setEnabled(true);
								mLoginStatusMessageView
										.setText("Wrong Username or password !");
							}
						});
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								mLoginStatusView.setVisibility(View.GONE);
							}
						}, 2000);
						Log.d("TAG",
								"Error in Authentication " + error.toString());
					}
				});
	}

}
