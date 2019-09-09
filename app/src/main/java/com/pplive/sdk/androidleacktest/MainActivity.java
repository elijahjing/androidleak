package com.pplive.sdk.androidleacktest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pplive.sdk.leacklibrary.activity.HeapActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.bottom_test);
        Button button2=findViewById(R.id.bottom_test2);

        final Intent intent=new  Intent(MainActivity.this,Main2Activity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(intent);
            }
        });
        final Intent intent2=new  Intent(MainActivity.this, HeapActivity.class);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(intent2);
            }
        });
    }
}
