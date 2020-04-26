package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        if (edtDOP.getText().toString().equals("Driver") || edtDOP.getText().toString().equals("Passenger")){
            if (ParseUser.getCurrentUser() == null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null){
                            Toast.makeText(MainActivity.this, "You are now logged in as an Anonymous User.", LENGTH_SHORT).show();

                            user.put("as",edtDOP.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                    transitionToDriverRequestListActivity();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    enum State {
        SIGNUP,LOGIN
    }

    private State state;
    private Button btnSignUpLogin,btnOneTimeLogin;
    private RadioButton driverRadioButton,passengerRadioButton;
    private EditText edtUserName,edtPassword,edtDOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ParseUser.getCurrentUser() != null){
          transitionToPassengerActivity();
          transitionToDriverRequestListActivity();
            //  ParseUser.logOut();
        }

        state = State.LOGIN;
        btnSignUpLogin = findViewById(R.id.btnSignUpLogin);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);

        driverRadioButton = findViewById(R.id.rdbDriver);
        passengerRadioButton = findViewById(R.id.rdbPassenger);

        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtDOP = findViewById(R.id.edtDOP);

        btnOneTimeLogin.setOnClickListener(this);

        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == State.SIGNUP){
                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false){
                        Toast.makeText(MainActivity.this, "Select one of the Options Given.", LENGTH_SHORT).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRadioButton.isChecked()){
                        appUser.put("as","Driver");
                    }else if(passengerRadioButton.isChecked()){
                        appUser.put("as","Passenger");
                    }

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null){
                                Toast.makeText(MainActivity.this, "Signed UP!", LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });
                }else if (state == State.LOGIN){
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(),
                            new LogInCallback() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    if (user != null && e == null){
                                        Toast.makeText(MainActivity.this, "User Logged In.", LENGTH_SHORT).show();
                                        transitionToPassengerActivity();
                                        transitionToDriverRequestListActivity();
                                    }
                                }
                            });
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_signup_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.loginItem:
                if (state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Login");

                }else if (state == State.LOGIN){
                    state = State.SIGNUP;
                    item.setTitle("Login");
                    btnSignUpLogin.setText("SignUp");

                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void transitionToDriverRequestListActivity(){

        if (ParseUser.getCurrentUser() != null ){
            if (ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(this,DriverRequestListActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

}
