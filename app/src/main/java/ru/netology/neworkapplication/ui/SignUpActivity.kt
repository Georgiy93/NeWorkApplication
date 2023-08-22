package ru.netology.neworkapplication.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.databinding.ActivitySignupBinding
import ru.netology.neworkapplication.viewmodel.AuthViewModel
import java.io.InputStream

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivitySignupBinding
    private lateinit var getImage: ActivityResultLauncher<String>
    private var avatarUrl: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            avatarUrl = uri
            binding.avatarImageView.setImageURI(uri) // this line updates the avatar in the ImageView
        }
        binding.apply {
            chooseAvatar.setOnClickListener {
                getImage.launch("image/*")
            }

            signUp.setOnClickListener {
                val login = createLogin.text.toString()
                val password = createPassword.text.toString()
                val name = createName.text.toString()
                val avatar = avatarUrl?.let(contentResolver::openInputStream)
                    ?.use(InputStream::readBytes)
                viewModel.register(login, password, name, avatar)
            }

        }

        viewModel.registrationResult.observe(this, { response ->
            if (response.responseBody != null) {

                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignUpActivity, AuthActivity::class.java)
                startActivity(intent)
                finish()
            } else {

                Toast.makeText(
                    this,
                    "Registration failed. Server response code: ${response.statusCode}, error message: ${response.errorMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


}

