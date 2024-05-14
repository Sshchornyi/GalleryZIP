package com.example.galleryzip;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Button compress_button, unzip_button, archive_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        compress_button = findViewById(R.id.compress_btn);
        unzip_button = findViewById(R.id.unzip_btn);
        archive_button = findViewById(R.id.archive_btn);

        compress_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransitiveActivity.class);
                intent.putExtra("operation","compress");
                startActivity(intent);
            }
        });
        archive_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransitiveActivity.class);
                intent.putExtra("operation","archive");
                startActivity(intent);
            }
        });

        unzip_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ZipSelectionTransitive.class);
                startActivity(intent);
            }
        });


    }

    private void moveToAnotherActivity(View v) {
        Intent intent = new Intent(this, TransitiveActivity.class);
        startActivity(intent);
    }


    private void showToastWindow(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

}