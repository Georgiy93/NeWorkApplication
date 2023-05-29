package ru.netology.neworkapplication.ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.neworkapplication.databinding.ActivitySignupBinding
import ru.netology.neworkapplication.dto.LoginRequest
import ru.netology.neworkapplication.dto.RegistrationRequest
import ru.netology.neworkapplication.viewmodel.AuthViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivitySignupBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            var avatarUrl = ""
            chooseAvatar.setOnClickListener {
                avatarUrl = avatarImageView.text.toString()
            }

            signUp.setOnClickListener {
                val login = createLogin.text.toString()
                val password = createPassword.text.toString()
                val name = createName.text.toString()

                viewModel.register(login, password, name, avatarUrl)
            }

        }

        viewModel.registrationResult.observe(this, { response ->
            if (response.responseBody != null) {
                // Registration was successful
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignUpActivity, AuthActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Registration failed
                Toast.makeText(
                    this,
                    "Registration failed. Server response code: ${response.statusCode}, error message: ${response.errorMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}

