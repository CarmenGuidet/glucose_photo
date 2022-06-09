package com.carmenguidetgomez.glucemyphoto.ui.login;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.carmenguidetgomez.glucemyphoto.Glucose;
import com.carmenguidetgomez.glucemyphoto.MainActivity;
import com.carmenguidetgomez.glucemyphoto.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    DatePickerDialog picker;
    RadioGroup gender;
    RadioButton female, male;
    public FirebaseAuth mAuth;
    String radiogender = "";


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            finish();
            return;
        }
        EditText etBirthDate = findViewById(R.id.etBirthDate);
        // ARREGLAR EL DATE PICKER PARA QUE NO DEVUELVA UN NULL
        etBirthDate.setInputType(InputType.TYPE_NULL);
        etBirthDate.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(RegisterActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        etBirthDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1);
                    }, year, month, day);
            picker.show();


        });


        String birthDate = etBirthDate.getText().toString();

        gender = (RadioGroup)findViewById(R.id.radioGroup);

        female = (RadioButton)findViewById(R.id.radioFemale);
        male = (RadioButton)findViewById(R.id.radioMale);

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                switch (gender.indexOfChild(findViewById(group.getCheckedRadioButtonId()))){
                    case 0:
                        radiogender = "Male";
                        break;
                    case 1:
                        radiogender = "Female";
                        break;
                }
            }
        });


        Spinner spinnerDiabetes=findViewById(R.id.spinner_tipodiabetes);

        ArrayAdapter<CharSequence>adapter= ArrayAdapter.createFromResource(this, R.array.typesOfDiabetes, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerDiabetes.setAdapter(adapter);

        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typeOfDiabetes = spinnerDiabetes.getSelectedItem().toString();
                registerUser(birthDate, radiogender, typeOfDiabetes);
            }
        });

        TextView textViewSwitchToLogin = findViewById(R.id.tvSwitchToLogin);
        textViewSwitchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToLogin();
            }
        });


    }


    private void registerUser(String stringbirthDate, String stringgender, String stringtypeOfDiabetes) {

        EditText etFirstName = findViewById(R.id.etFirstName);
        EditText etLastName = findViewById(R.id.etLastName);
        EditText etRegisterEmail = findViewById(R.id.etRegisterEmail);
        EditText etRegisterPassword = findViewById(R.id.etRegisterPassword);
        String gender = stringgender;
        String birthDate = stringbirthDate;
        String type = stringtypeOfDiabetes;
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String email = etRegisterEmail.getText().toString();
        String password = etRegisterPassword.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference users = db.collection("users");
                            DocumentReference newUser = users.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            newUser.set(new Users(firstName, lastName, email, birthDate, gender, type))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            showMainActivity();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
