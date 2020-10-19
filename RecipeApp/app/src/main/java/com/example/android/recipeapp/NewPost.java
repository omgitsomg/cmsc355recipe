package com.example.android.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import java.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Calendar;

/*******************************************************************************************
 * The new post page when button is clicked on main screen.
 *****************************************NOTES*********************************************
 * Need to transfer information into profile page
 * Link to post button on main screen
 *
 *******************************************************************************************/

public class NewPost extends AppCompatActivity
{
    private ImageButton selectImage;
    private Button addIngredientsButton,addInstructionsButton,addNutritionFacts,postButton;
    private EditText captionBox;
    private String description;
    private String saveCurrentDate, saveCurrentTime, postRandomName;

    private StorageReference postImageReference;
    private Uri image;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private static final int GALLERYPIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        postImageReference = FirebaseStorage.getInstance().getReference().child("Post Images");
        selectImage = (ImageButton) findViewById(R.id.imageButton);
        addIngredientsButton = (Button) findViewById(R.id.ingredientsButton);
        addInstructionsButton = (Button) findViewById(R.id.instructionsButton);
        addNutritionFacts = (Button) findViewById(R.id.nutritionButton);
        postButton = (Button) findViewById(R.id.postButton);
        captionBox = (EditText) findViewById(R.id.descriptionBox);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String userID = user.getUid();
        myRef = mFirebaseDatabase.getReference().child("Users").child(userID);


        /*******************************************************************************************
         * Button to upload an image (includes camera/gallery option)
         *******************************************************************************************/
         selectImage.setOnClickListener(new View.OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 OpenGallery();
             }
         });

         /*******************************************************************************************
        * Button to add the post to the Firebase and also the home screen
         *******************************************************************************************/
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
                postRecipe(userID);
            }
        });
        /*******************************************************************************************
         * Button to navigate to add ingredients page
         *******************************************************************************************/
        addIngredientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ingredientsIntent = new Intent(getApplicationContext(), AddIngredients.class);
                startActivity(ingredientsIntent);
            }
        });

        /*******************************************************************************************
         * Button to navigate to add instructions page
         *******************************************************************************************/

        addInstructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent instructionsIntent = new Intent(getApplicationContext(), AddInstructions.class);
                startActivity(instructionsIntent);
            }
        });

        /*******************************************************************************************
         * Button to navigate to add nutrition facts page
         *******************************************************************************************/
        addNutritionFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nutritionIntent = new Intent(getApplicationContext(), NutritionEdit.class);
                startActivity(nutritionIntent);
            }
        });

    }

    private void ValidatePostInfo()
    {
        description = captionBox.getText().toString();
        if(image == null)
        {
            Toast.makeText(NewPost.   this, "Please select an image for your post ",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(NewPost.   this, "Please write a description of your recipe ",Toast.LENGTH_SHORT).show();
        }
        else
        {
            ImagetoFirebase();
            SendUsertoMain();
        }

    }

    private void SendUsertoMain()
    {
        Intent sendUsertoMain = new Intent(NewPost.this, MainActivity.class);
        sendUsertoMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendUsertoMain);
        finish();
    }

    private void ImagetoFirebase()
    {
        Calendar callforDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
        saveCurrentDate = currentDate.format(callforDate.getTime());

        Calendar callforTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callforTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filepath = postImageReference.child("Post Images").child(image.getLastPathSegment() + postRandomName + ".jpg");
        filepath.putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(NewPost.this, "Image uploaded successfully",Toast.LENGTH_LONG).show();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(NewPost.this, "Error: " + message ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERYPIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERYPIC && resultCode == RESULT_OK && data != null)
        {
            image = data.getData();
            selectImage.setImageURI(image);

        }
    }

    private void postRecipe (String userID)
    {
        // Get values from other activities
        // Initialize the variables to the values
        EditText foodName = (EditText) findViewById(R.id.editTextTextMultiLine);

        String recipeName = foodName.getText().toString();
        if (!recipeName.equals(""))
        {
            myRef.child("recipes").child(recipeName).setValue("true");
            Toast.makeText(NewPost.this, "Posting " + recipeName + ".", Toast.LENGTH_LONG).show();

            foodName.setText(""); // This will reset the text.
        }

        String ingredients = getIntent().getStringExtra("ingredients");
        String instructions = getIntent().getStringExtra("instructions");
        String description = captionBox.toString();

    }

}