package com.example.galleryzip;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSelectionTransitive extends AppCompatActivity {

    TextView textView1;
    ImageView back_btn;
    Button continue_btn;
    File zipFile;
    private static final int Read_Permission = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_zip_selection_transitive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView1 = findViewById(R.id.selected_file);
        back_btn = findViewById(R.id.back_btn_file_selector);
        continue_btn = findViewById(R.id.continue_btn_file_selector);


        if (ContextCompat.checkSelfPermission(ZipSelectionTransitive.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ZipSelectionTransitive.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Оберіть архів"), 100);
        } catch (Exception e) {
            Toast.makeText(this, "Помилка при виборі архіву", Toast.LENGTH_SHORT).show();
        }
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    unzip();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String path = uri.getPath();
            zipFile = new File(path);

            textView1.setText(zipFile.getName());

        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void unzip() throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
/*        File folder = new File(downloadsDir, "GalleryZIP");

        // Перевірка, чи папка вже існує, якщо ні - створюємо її
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(this, "Помилка при створенні папки", Toast.LENGTH_SHORT).show();
                return;
            }
        }*/

        byte[] buffer = new byte[1024];
        String path = zipFile.getPath().substring(zipFile.getPath().lastIndexOf(":")+1);
        FileInputStream inputStream = new FileInputStream(path);

        ZipInputStream zis = new ZipInputStream(inputStream);

        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {

            File newFile = new File(downloadsDir, zipEntry.getName());

            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
            }

            // write file content
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();


            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
    private void showProcess(String text) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(ZipSelectionTransitive.this);
        alertDlgBuilder.setTitle("AlertDialog")
                .setCancelable(true)
                .setPositiveButton("Добре", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ZipSelectionTransitive.this, "Ви не обрали жодного зображення!", Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("Скасувати", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setMessage(text);
        AlertDialog dialog = alertDlgBuilder.create();
        dialog.show();
    }
}