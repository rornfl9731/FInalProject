package com.bluebead38.opencvtesseractocr;
//리뷰작성 액티비티

//import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class Review_In_Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button reg_btn;
    private EditText content_txt;
    private EditText title_txt;
    private ImageView upload_view;


    String mStoragePath = "All_Image_Uploads/";
    //Root Database name for firebase database
    String mDatabasePath = "Data";

    //Creating URI
    Uri mFilePathUri;

    //Creating StorageReference and Database reference
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    //ProgressDialog
    //ProgressDialog mProgressDialog;

    //Image request code for choosing image
    int IMAGE_REQUEST_CODE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.review_in_lay);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reg_btn = (Button)findViewById(R.id.upload_btn);
        title_txt = (EditText) findViewById(R.id.title_txt);
        content_txt = (EditText)findViewById(R.id.content_txt);
        upload_view = (ImageView) findViewById(R.id.uploadima_view);

        upload_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                //setting intent type as image to select image from phone storage
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);

            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDataToFirebase();


            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();
        //assign FirebaseDatabase instance with root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(mDatabasePath);

        //progress dialog
       // mProgressDialog = new ProgressDialog(Review_In_Activity.this);









        




//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }



    private void uploadDataToFirebase() {
        //check whether filepathuri is empty or not
        if (mFilePathUri != null) {
            //setting progress bar title
          //  mProgressDialog.setTitle("Uploading...");
            //show progress dialog
           // mProgressDialog.show();
            //create second storageReference
            StorageReference storageReference2nd = mStorageReference.child(mStoragePath + System.currentTimeMillis() + "." + getFileExtension(mFilePathUri));

            //adding addOnSuccessListener to storageReference2nd
            storageReference2nd.putFile(mFilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            //get title
                            String mPostTitle = title_txt.getText().toString().trim();
                            //get description
                            String mPostDescr = content_txt.getText().toString().trim();
                            //hid progress dialog
                           // mProgressDialog.dismiss();
                            //show toast that image is uploaded
                            Toast.makeText(Review_In_Activity.this, "Uploaded successfully...", Toast.LENGTH_SHORT).show();
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mPostTitle, mPostDescr, downloadUri.toString(), mPostTitle.toLowerCase());
                            //getting image upload id
                            String imageUploadId = mDatabaseReference.push().getKey();
                            //adding image upload id's child element into databaseRefrence
                            mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    //if something goes wrong such as network failure etc
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //hide progress dialog
                            //mProgressDialog.dismiss();
                            //show error toast
                            Toast.makeText(Review_In_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                          //  mProgressDialog.setTitle("Uploading...");
                        }
                    });
        } else {
            Toast.makeText(this, "Please select image or add image name", Toast.LENGTH_SHORT).show();
        }
    }



    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        //returning the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this,PostListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this,Review_In_Activity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            mFilePathUri = data.getData();

            try {
                //getting selected image into bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);
                //setting bitmap into imageview
                upload_view.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
