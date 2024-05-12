package com.example.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameEditText = findViewById(R.id.user_name_edit_text);
        goButton = findViewById(R.id.go_button);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEditText.getText().toString();
                // Start the chat activity and pass the user name
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });
    }
}

