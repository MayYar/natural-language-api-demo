package com.example.googleNLP;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    ImageButton helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = (ImageButton)findViewById(R.id.ib_helper);
        helper.setOnClickListener(doClick);
    }

    private Button.OnClickListener doClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
            startActivity(intent);
        }
    };
}
