//**************************************************************
// Project: OnyxRidge Construction Management
//
// File: UploadExistingReportFragment.java
//
// Written by: Raymond O'Neill
//
// Written on: ~11/6/2020
//
// Purpose: Used to upload pre-existing reports to Storage
// ------------------------------------------------
// UPDATES
// ------------------------------------------------
// - 11/8/20
// - R.O.
// - DETAILS:
//      - Handled actual uploading and metadata insertion into files
// ------------------------------------------------
// - 11/10/20
// - R.O.
// - DETAILS:
//      - Removed duplicate code for the job name spinner that
//        added an OnItemSelectedListener, that did the same
//        thing, twice.
//      - Changed a invoke to getSupportFragmentManager(...)
//        to a variable name (instantiated early in method).
//      - Added brackets around an if(...) statement to prevent
//        logic error
// ------------------------------------------------
// - 11/12/20
// - R.O.
// - DETAILS:
//      - Added a `.load()` method call to the PDFView so it
//        actually shows PDF before user uploads
//**************************************************************
package com.icecrown.onyxridgecm.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.icecrown.onyxridgecm.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UploadExistingReportFragment extends Fragment {
    private String jobNameValue = "";
    private final int PICK_FILE_TO_UPLOAD = 0;
    private Uri chosenFile = null;
    private PDFView pdfView;
    private MaterialTextView workingLoadingTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upload_existing_files, container, false);

        StorageReference rootRef = FirebaseStorage.getInstance().getReference();

        pdfView = v.findViewById(R.id.photo_preview_viewer_image_view);
        workingLoadingTextView = v.findViewById(R.id.working_and_loading_text_view);

        MaterialButton pickFileButton = v.findViewById(R.id.select_file_from_explorer);

        pickFileButton.setOnClickListener(l -> OpenFileFromDirectory());

        final Spinner jobNameSpinner = v.findViewById(R.id.job_name_spinner);
        jobNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                jobNameValue = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rootRef.listAll().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> jobNames = new ArrayList<>();

                jobNames.add(getString(R.string.job_name_default_value));
                for (StorageReference ref : task.getResult().getPrefixes()) {
                    jobNames.add(ref.getName());
                }
                ArrayAdapter<String> jobNamesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, jobNames);
                jobNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                jobNameSpinner.setAdapter(jobNamesAdapter);
            }
            else {
                Snackbar.make(jobNameSpinner, R.string.projects_not_loaded, Snackbar.LENGTH_SHORT).show();
            }
        });


        MaterialButton uploadFileToStorageButton = v.findViewById(R.id.upload_file_to_storage_button);
        uploadFileToStorageButton.setOnClickListener(l -> {
            if(jobNameValue.isEmpty() || jobNameValue.equals(getString(R.string.job_name_default_value)) || jobNameValue.equals(""))
            {
                Snackbar.make(jobNameSpinner, R.string.no_project_chosen_upload, Snackbar.LENGTH_SHORT).show();
            }
            else {
                if(chosenFile == null || chosenFile.getPath().equals("")) {
                    Snackbar.make(jobNameSpinner, R.string.no_file_chosen_for_upload, Snackbar.LENGTH_SHORT).show();
                }
                else {

                    StorageReference fileDep = rootRef.child(jobNameValue + "/documents/" + new File(chosenFile.getLastPathSegment()).getName());
                    fileDep.putFile(chosenFile).addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Calendar cal = Calendar.getInstance();

                            final String todaysDate = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR); // Calendar.MONTH is offset by 1 (?) (no explanation why)

                            SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("application/pdf")
                                    .setCustomMetadata("first_name", prefs.getString("first_name", "Unavail."))
                                    .setCustomMetadata("last_name", prefs.getString("last_name", "Unavail."))
                                    .setCustomMetadata("date_created", todaysDate)
                                    .setCustomMetadata("date_of_content", todaysDate)
                                    .setCustomMetadata("document_name", chosenFile.getLastPathSegment())
                                    .setCustomMetadata("accident_happened", "false").build();
                            fileDep.updateMetadata(metadata).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Snackbar.make(jobNameSpinner, R.string.report_upload_success, Snackbar.LENGTH_SHORT).show();
                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                    if (manager.getBackStackEntryCount() != -1) {
                                        manager.popBackStack();
                                    }
                                } else {
                                    Snackbar.make(jobNameSpinner, R.string.metatdata_not_applied_admin, Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else {
                            Snackbar.make(jobNameSpinner, R.string.upload_report_failed, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return v;
    }

    private void OpenFileFromDirectory() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/pdf");
        i.putExtra(DocumentsContract.EXTRA_INITIAL_URI, chosenFile);
        startActivityForResult(i, PICK_FILE_TO_UPLOAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FILE_TO_UPLOAD && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                chosenFile = data.getData();
                Log.d("EPOCH-3", "File name: " + chosenFile.getPath());
                workingLoadingTextView.setText(R.string.loading_in_progress);
                pdfView.fromUri(chosenFile).load();
                workingLoadingTextView.setText("");
            }
        }
    }
}
