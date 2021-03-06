package edu.cnm.deepdive.gallery.controller;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.squareup.picasso.Picasso;
import edu.cnm.deepdive.gallery.R;
import edu.cnm.deepdive.gallery.databinding.FragmentUploadPropertiesBinding;
import edu.cnm.deepdive.gallery.viewmodel.MainViewModel;

public class UploadPropertiesFragment extends DialogFragment implements TextWatcher {

  private FragmentUploadPropertiesBinding binding;
  private Uri uri;
  private AlertDialog dialog;
  private MainViewModel viewModel;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    //noinspection ConstantConditions
    uri = UploadPropertiesFragmentArgs.fromBundle(getArguments()).getContentUri();
    binding =
        FragmentUploadPropertiesBinding.inflate(LayoutInflater.from(getContext()), null, false);
    dialog = new Builder(getContext())
        .setIcon(R.drawable.ic_image)
        .setTitle(R.string.upload_properties_title)
        .setView(binding.getRoot())
        .setNeutralButton(android.R.string.cancel, (dlg, which) -> {})
        .setPositiveButton(android.R.string.ok, (dlg, which) -> upload())
        .create();
    dialog.setOnShowListener((dlg) -> checkSubmitConditions());
    return dialog;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Picasso.get()
        .load(uri)
        .into(binding.image);
    binding.title.addTextChangedListener(this);
    //noinspection ConstantConditions
    viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
  }

  @Override
  public void afterTextChanged(Editable s) {
    checkSubmitConditions();
  }

  private void checkSubmitConditions() {
    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    //noinspection ConstantConditions
    positive.setEnabled(!binding.title.getText().toString().trim().isEmpty());
  }

  @SuppressWarnings("ConstantConditions")
  private void upload() {
    String title = binding.title.getText().toString().trim();
    String description = binding.description.getText().toString().trim();
    viewModel.storeImage(uri, title, (description.isEmpty() ? null : description));
  }

}
