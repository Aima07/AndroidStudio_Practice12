package id.sttbandung.practice12

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.annotation.Nullable
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

class NewsAdd : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private var id: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button
    private var imageUri: Uri? = null

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_add)

        // Initialize Firebase
        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI components
        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)

        progressDialog = ProgressDialog(this@NewsAdd)
        progressDialog.setTitle("Loading...")

        chooseImage.setOnClickListener {
            openFileChooser()
        }
        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra("id")
            judul = updateOption.getStringExtra("title")
            deskripsi = updateOption.getStringExtra("desc")
            image = updateOption.getStringExtra("imageUrl")

            title.setText(judul)
            desc.setText(deskripsi)
            Glide.with(this).load(image).into(imageView)
        }
        saveNews.setOnClickListener {
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()

            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(
                    this@NewsAdd,
                    "Title and Description cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            progressDialog.show()
            if (imageUri != null) {
                uploadImageToStorage(newsTitle, newsDesc)
            } else {
                saveData(newsTitle, newsDesc, image ?: "")

            }
        }

    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
        imageUri?.let { uri ->
            val storageRef =
                storage.reference.child("news_images/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        saveData(newsTitle, newsDesc, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewsAdd,
                        "Failed to upload image: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = HashMap<String, Any>()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["imageUrl"] = imageUrl

        if (id != null) {
            dbNews.collection("news").document(id ?: "")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "News Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "News updating news: " + e.message, Toast.LENGTH_SHORT)
                        .show()
                    Log.w("NewsAdd", "Error updating document", e)
                }
        } else {
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd, "News added successfully", Toast.LENGTH_SHORT)
                        .show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0) // Clear the imageView
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewsAdd,
                        "Error adding news : &{e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
}