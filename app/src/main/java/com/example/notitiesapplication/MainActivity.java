package com.example.notitiesapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton add_button;
    FloatingActionButton deleteAllButton;
    EditText editTextSearch;

    MyDataBaseHelper myDB;
    ArrayList<String> note_id, note_title, note_input;

    CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        add_button = findViewById(R.id.add_button);
        deleteAllButton = findViewById(R.id.floatingActionButton);
        editTextSearch = findViewById(R.id.editTextSearch);

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllNotes();
            }
        });

        myDB = new MyDataBaseHelper(MainActivity.this);
        note_id = new ArrayList<>();
        note_title = new ArrayList<>();
        note_input = new ArrayList<>();

        storeDataInArrays();

        customAdapter = new CustomAdapter(MainActivity.this, note_id, note_title, note_input);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Search functionality
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                filter(editTextSearch.getText().toString());
                return true;
            }
            return false;
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                note_id.add(cursor.getString(0));
                note_title.add(cursor.getString(1));
                note_input.add(cursor.getString(2));
            }
        }
    }

    private void deleteAllNotes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Voulez-vous vraiment supprimer toutes les notes?");
        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myDB.deleteAllData();
                note_id.clear();
                note_title.clear();
                note_input.clear();
                customAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "All notes deleted.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing if the user clicks "Non"
            }
        });
        builder.show();
    }

    private void filter(String text) {
        if (TextUtils.isEmpty(text)) {
            updateRecyclerView(); // Display all notes when search text is empty
        } else {
            ArrayList<String> filteredNoteId = new ArrayList<>();
            ArrayList<String> filteredNoteTitle = new ArrayList<>();
            ArrayList<String> filteredNoteInput = new ArrayList<>();

            for (int i = 0; i < note_title.size(); i++) {
                if (note_title.get(i).toLowerCase().contains(text.toLowerCase())) {
                    filteredNoteId.add(note_id.get(i));
                    filteredNoteTitle.add(note_title.get(i));
                    filteredNoteInput.add(note_input.get(i));
                }
            }

            customAdapter.filterList(filteredNoteId, filteredNoteTitle, filteredNoteInput);
        }
    }

    void updateRecyclerView() {
        note_id.clear();
        note_title.clear();
        note_input.clear();

        storeDataInArrays();

        customAdapter = new CustomAdapter(MainActivity.this, note_id, note_title, note_input);
        recyclerView.setAdapter(customAdapter);
    }
}
