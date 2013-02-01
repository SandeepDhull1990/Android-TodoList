package com.example.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appacitive.android.callbacks.AppacitiveSignUpCallback;
import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveUser;
import com.appacitive.android.model.AppacitiveUserDetail;

public class SignUpActivity extends Activity {

	private EditText mUserNameView;
	private EditText mEmailView;
	private EditText mFirstNameView;
	private EditText mPasswordView;
	private View mSignupStatusView;
	private TextView mSignUpStatusMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		initSubview();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent upIntent = new Intent(this, LoginActivity.class);
			NavUtils.navigateUpTo(this, upIntent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initSubview() {
		mUserNameView = (EditText) findViewById(R.id.signup_username);
		mEmailView = (EditText) findViewById(R.id.signup_email);
		mFirstNameView = (EditText) findViewById(R.id.signup_firstname);
		mPasswordView = (EditText) findViewById(R.id.signup_password);
		mSignupStatusView = findViewById(R.id.signup_status);
		mSignUpStatusMessage = (TextView) findViewById(R.id.signup_status_message);
	}
	
	public void onSignUpButtonClick(View v) {
		String userName = mUserNameView.getText().toString().trim();
		String email = mEmailView.getText().toString().trim();
		String firstName = mFirstNameView.getText().toString().trim();
		String password = mPasswordView.getText().toString().trim();
		signUp(userName, email , firstName, password);
	}
	
	private void signUp(String userName, String email, String firstName, String password){ 
		AppacitiveUserDetail userDetail = new AppacitiveUserDetail();
		userDetail.setUserName(userName);
		userDetail.setEmail(email);
		userDetail.setFirstName(firstName);
		userDetail.setPassword(password);
		
		mSignupStatusView.bringToFront();
		mSignUpStatusMessage.setText("Signing Up");
		mSignupStatusView.setVisibility(View.VISIBLE);
		
		AppacitiveUser.createUser(userDetail, new AppacitiveSignUpCallback() {
			
			@Override
			public void onSuccess(AppacitiveUser user) {
				Intent upIntent = new Intent(SignUpActivity.this, LoginActivity.class);
				NavUtils.navigateUpTo(SignUpActivity.this, upIntent);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(getBaseContext(), "Sign up successfull", Toast.LENGTH_SHORT).show();
						mSignupStatusView.setVisibility(View.GONE);
					}
				});
			}
			
			@Override
			public void onFailure(final AppacitiveError error) {
				Log.w("TAG", "Error while creating the user " + error.toString());
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mSignupStatusView.setVisibility(View.GONE);
						if(error.getStatusCode().equals("600")) {
							mUserNameView.setError("User already exist");
						}
					}
				});
			}
		});
		
	}
}
