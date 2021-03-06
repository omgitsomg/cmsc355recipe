package com.example.android.recipeapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

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
    private EditText captionBox, recipeNameBox, ingredientsBox, instructionsBox;
    private String description, recipeName, imageuri, instructions, ingredients;
    private Button goToNutritionBtn, backButton;

    private StorageReference postImageReference;
    private Uri image;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private static final int GALLERYPIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        postImageReference = FirebaseStorage.getInstance().getReference().child("PostImages"); // creates a folder
        selectImage = findViewById(R.id.imageButton);
        captionBox = findViewById(R.id.descriptionBox);
        recipeNameBox = findViewById(R.id.RecipeName);
        ingredientsBox = findViewById(R.id.ingredientsBox);
        instructionsBox = findViewById(R.id.instructionsBox);
        goToNutritionBtn = findViewById(R.id.goToNutrition);
        backButton = findViewById(R.id.backbutton);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String userID = user.getUid();
        myRef = mFirebaseDatabase.getReference().child("Users").child(userID);

        /*******************************************************************************************
         * Button to upload an image (includes camera/gallery option)
         *******************************************************************************************/
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        goToNutritionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidatePostInfo();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(NewPost.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    /*******************************************************************************************
     * Checks if the User has entered a all required texts
     * of the recipe.
     *******************************************************************************************/

    private void ValidatePostInfo()
    {
        description = captionBox.getText().toString();
        recipeName = recipeNameBox.getText().toString();
        instructions = instructionsBox.getText().toString();
        ingredients = ingredientsBox.getText().toString();

        if(image == null)
        {
            Toast.makeText(NewPost.   this, "Please select an image for your post ",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(recipeName))
        {
            Toast.makeText(NewPost.   this, "Please Enter the Title of your recipe ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description))
        {
            Toast.makeText(NewPost.   this, "Please write a description of your recipe ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(ingredients))
        {
            Toast.makeText(NewPost.   this, "Please Enter the Ingredients of your recipe ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(instructions))
        {
            Toast.makeText(NewPost.   this, "Please Enter the Instructions of your recipe ",Toast.LENGTH_SHORT).show();
        }
        else
        {
            SendToNutrition();
        }

    }

    /*******************************************************************************************
     * SendToNutrition will send the recipe name, description, and image
     * to the NutritionEdit activity.
     *******************************************************************************************/

    private void SendToNutrition() {
        Intent intent = new Intent(NewPost.this, NutritionEdit.class);
        intent.putExtra("recipeName", recipeName);
        intent.putExtra("description",description);
        intent.putExtra("image", imageuri); //This is the String of the Uri image. Remember to convert to Uri at the end
        intent.putExtra("ingredients", ingredients);
        intent.putExtra("instructions", instructions);
        startActivity(intent);
    }

    @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == GALLERYPIC)
            {
                if(resultCode == RESULT_OK)
                {
                    image = data.getData();
                    selectImage.setImageURI(image);

                    final StorageReference filepath = postImageReference.child("PostImages" + image.getLastPathSegment()); // retrieving from folder

                    filepath.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    Toast.makeText(NewPost.   this, "Image Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                    imageuri = uri.toString();

                                }
                            });

                        }
                    });

                }
            }
        }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERYPIC);
    }
}