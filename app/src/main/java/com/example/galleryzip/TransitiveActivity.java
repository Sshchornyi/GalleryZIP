package com.example.galleryzip;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class TransitiveActivity extends AppCompatActivity implements RecyclerAdapter.CountOfImagesWhenRemoved {


    RecyclerView recyclerView;
    TextView textView;
    ImageView back_btn;
    Button continue_btn;

    ArrayList<Uri> uri = new ArrayList<>();
    RecyclerAdapter adapter;
    private static final int Read_Permission = 101;
    private static final int SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transitive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        textView = findViewById(R.id.selected_images_str);
        recyclerView = findViewById(R.id.recyclerViewImages);
        back_btn = findViewById(R.id.back_btn);
        continue_btn = findViewById(R.id.continue_btn);
        adapter = new RecyclerAdapter(uri, getApplicationContext(), this);

        recyclerView.setLayoutManager(new GridLayoutManager(TransitiveActivity.this, 4));
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(TransitiveActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TransitiveActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Read_Permission);
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        startActivityForResult(Intent.createChooser(intent, "Оберіть зображення"), SELECT_IMAGE);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = getIntent().getExtras();

                if (extras != null) {
                    String operation = extras.getString("operation");

                    if (operation != null && operation.equals("compress")) {

                        try {
                            compress();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (operation != null && operation.equals("archive")) {
                        archive();

                    }

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE && resultCode == MainActivity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int img_counter = data.getClipData().getItemCount();
                for (int i = 0; i < img_counter; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uri.add(imageUri);
                }
                adapter.notifyDataSetChanged();
                textView.setText("Обрано: " + uri.size());
            } else {
                Uri imageUri = data.getData();
                uri.add(imageUri);
            }
            adapter.notifyDataSetChanged();
            textView.setText("Обрано: " + uri.size());
        } else {
            Toast.makeText(this, "Ви не обрали жодного зображення!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TransitiveActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void clicked(int getSize) {
        textView.setText("Обрано: " + uri.size());

    }

    private void compress() throws IOException {
        // Обрання місця для зберігання
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      /*  File folder = new File(downloadsDir, "GalleryZIP");

        // Перевірка, чи папка вже існує, якщо ні - створюємо її
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(this, "Помилка при створенні папки", Toast.LENGTH_SHORT).show();
                return;
            }
        }*/
        for (int i = 0; i < uri.size(); i++) {

            Uri imageUri = uri.get(i);
            String extension = getFileExtension(getPathFromUri(imageUri));
            //String uriNameAndExtension = getContentResolver().getType(imageUri);
            //String extension= uriNameAndExtension.substring(uriNameAndExtension.lastIndexOf("/")+1);
            //showProcess(extensionNormal);


            if (extension.equalsIgnoreCase("png")) {
                try {

                    PngImage inputImage = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        inputImage = new PngImage(Files.newInputStream(Paths.get(getPathFromUri(imageUri))));
                    }

                    PngOptimizer optimizer = new PngOptimizer();
                    PngImage optimizedImage = optimizer.optimize(inputImage);

                    String uniqueFileName = "optimized_image_" + System.currentTimeMillis() + ".png";
                    String outputFilePath = downloadsDir.getAbsolutePath() + File.separator + uniqueFileName;

                    OutputStream output = new FileOutputStream(outputFilePath);
                    optimizedImage.writeDataOutputStream(output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
                Bitmap bitmap = BitmapFactory.decodeFile(getPathFromUri(imageUri));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // 50 - рівень стиснення
                byte[] imageBytes = outputStream.toByteArray();

                String uniqueFileName = "optimized_image_" + System.currentTimeMillis() + ".jpg";
                String outputFilePath = downloadsDir.getAbsolutePath() + File.separator + uniqueFileName;

                FileOutputStream fos = new FileOutputStream(outputFilePath);
                fos.write(imageBytes);
                fos.close();


            } else {
                Toast.makeText(TransitiveActivity.this, "Деякі зображення не було опрацьовано через непідтримуваний формат", Toast.LENGTH_LONG);
            }
        }
        showDone("Стиснення завершено!");

    }

    private void archive() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

/*
        File folder = new File(downloadsDir, "GalleryZIP");

        // Перевірка, чи папка вже існує, якщо ні - створюємо її
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Toast.makeText(this, "Помилка при створенні папки", Toast.LENGTH_SHORT).show();
                return;
            }
        }
*/

        String fileName = "archivedImages_" + System.currentTimeMillis() + ".zip";
        File zipFile = new File(downloadsDir, fileName);

        try {
            // Ініціалізація ZipOutputStream для запису у ZIP-файл
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            // Додавання кожного обраного зображення до ZIP-файлу
            for (Uri imageUri : uri) {
                // Отримання шляху до зображення
                String imagePath = getPathFromUri(imageUri);

                // Генерування ім'я файлу + визначення розширення
                String imageName = "image_" + System.currentTimeMillis()+"."
                        + getContentResolver().getType(imageUri)
                        .substring(getContentResolver()
                                .getType(imageUri).lastIndexOf("/")+1);

                // Створення нового ZIP-запису
                zipOutputStream.putNextEntry(new ZipEntry(imageName));

                // Копіювання зображення у ZIP-файл
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                zipOutputStream.closeEntry();
                inputStream.close();
            }

            // Закриття ZipOutputStream
            zipOutputStream.close();

            // Повідомлення користувачу про успішну архівацію
            if (uri.size()>1)
                showDone("Зображення успішно архівовані  в Завантаження" );//+ zipFile.getAbsolutePath())
            else
                showDone("Зображення успішно архівовано  в Завантаження");

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Помилка при архівації зображень", Toast.LENGTH_SHORT).show();
        }
    }

    private String getPathFromUri(Uri uri) {
        String filePath = "";
        try {

            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            File file = new File(getCacheDir(), "temp_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4 * 1024]; // or other buffer size
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            filePath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    private String getFileExtension(String filePath) {
        String extension = null;
        if (filePath != null) {
            String filename = filePath.substring(filePath.lastIndexOf('\\') + 1);

            // find the last occurrence of '.' in the filename
            int dotIndex = filename.lastIndexOf('.');

            extension = (dotIndex > 0) ? filename.substring(dotIndex + 1) : "";
        }
        return extension;
    }

    private void showDone(String text) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(TransitiveActivity.this);
        alertDlgBuilder.setTitle(text)
                .setCancelable(true)
                .setPositiveButton("Готово", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });

        AlertDialog dialog = alertDlgBuilder.create();
        dialog.show();
    }

    private void showProcess(String text) {
        AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(TransitiveActivity.this);
        alertDlgBuilder.setTitle("AlertDialog")
                .setCancelable(true)
                .setPositiveButton("Добре", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(TransitiveActivity.this, "Ви не обрали жодного зображення!", Toast.LENGTH_LONG).show();

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