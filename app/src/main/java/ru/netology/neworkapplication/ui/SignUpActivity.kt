package ru.netology.neworkapplication.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.databinding.ActivitySignupBinding
import ru.netology.neworkapplication.dto.LoginRequest
import ru.netology.neworkapplication.dto.RegistrationRequest
import ru.netology.neworkapplication.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivitySignupBinding
    private var avatarUri: String? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri? ->
        uri?.let {
            binding.avatarImageView.setImageURI(uri)
            avatarUri = uri.toString()  // change here
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            chooseAvatar.setOnClickListener {
                openImagePicker()
            }
            signUp.setOnClickListener {
                val login = createLogin.text.toString()
                val password = createPassword.text.toString()
                val name = createName.text.toString()
                val avatar = avatarUri
                if (login.isNotBlank() && password.isNotBlank()) {
                    val registerRequest = RegistrationRequest(login, password, name, avatar)
                    viewModel.register(registerRequest)
                } else {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please fill out all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

//        viewModel.registrationLoading.observe(this, { isLoading ->
//            binding.registrationProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
//        })

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


        viewModel.registrationError.observe(this, { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun openImagePicker() {
        pickImage.launch("image/*")
    }

}
