package com.droid.snapchatoreo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*


class SnapCreateActivity : AppCompatActivity() {
     var createSnapImage: ImageView? = null
    var messageEditText: EditText? = null
    val imageName = UUID.randomUUID().toString() +".jpg"

    fun getPhoto() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getPhoto()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snap_create)
        createSnapImage = findViewById(R.id.imageView)
        messageEditText = findViewById(R.id.messageEditText)
    }
    fun chooseImageClicked(view: View){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            getPhoto()
        }
    }
    fun onNextClicked(view: View){
        createSnapImage?.isDrawingCacheEnabled = true
        createSnapImage?.buildDrawingCache()
        val bitmap = (createSnapImage?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

          // making a folder for images in firebase storage............  FirebaseStorage.getInstance().getReference().child("SnapImages").child(imageName)

        var uploadTask =  FirebaseStorage.getInstance().getReference().child("SnapImages").child(imageName).putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "Upload Failed!!", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            val url: Uri
            val uri: Task<Uri> =
                taskSnapshot.storage.downloadUrl
            while (!uri.isComplete());
            url = uri.getResult()!!
            Log.i("URL",url.toString())
            val intent = Intent(this, ChooseUserActivity::class.java)
            intent.putExtra("imageURL",url.toString())
            intent.putExtra("imageName",imageName)
            intent.putExtra("message", messageEditText?.text.toString())
            startActivity(intent)
        }

    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        val selectImage = data!!.data
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectImage)
                createSnapImage?.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
