package com.carmenguidetgomez.glucemyphoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.carmenguidetgomez.glucemyphoto.ui.login.LoginActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.opencv.android.OpenCVLoader;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private CollectionReference notebookRef = db.collection("Glucose");
    String ordenar = "date";
    private final CollectionReference notebookRef = db.collection("users")
            .document(currentUser.getUid())
            .collection("samples");

    private GlucoseAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FloatingActionButton Add = findViewById(R.id.floatingActionButtonAdd);
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( MainActivity.this, ProcessedActivity.class));
            }
        });

        FloatingActionButton Info = findViewById(R.id.floatingActionButtonInfo);
        Info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });


        setUpRecyclerView(ordenar);

        if(OpenCVLoader.initDebug()){
            System.out.println("OPENCV IS WORKING");
        }else{
            System.out.println("OPENCV IS NOT WORKING");
        }
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.Logout:
                signOut();
                return true;

            case R.id.Añadir_muestra:
                startActivity(new Intent( MainActivity.this, ProcessedActivity.class));
                return true;

            case R.id.info_app:
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
                return true;

            /***********
            case R.id.Ordenar_fecha:
                ordenar = "date";
                setUpRecyclerView(ordenar);

            case R.id.Ordenar_glucosa:
                ordenar = "glucose";
                setUpRecyclerView(ordenar);
            ****/
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setUpRecyclerView(String ordenar) {
        String ordenar2 = ordenar;
        Query query = notebookRef.orderBy(ordenar2, Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Glucose> options = new FirestoreRecyclerOptions.Builder<Glucose>()
                .setQuery(query, Glucose.class)
                .build();

        adapter = new GlucoseAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT
                | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAbsoluteAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);


    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.startListening();
    }
    private void signOut() {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}