package edu.cnm.deepdive.gallery.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import edu.cnm.deepdive.gallery.NavGraphDirections;
import edu.cnm.deepdive.gallery.NavGraphDirections.OpenUploadProperties;
import edu.cnm.deepdive.gallery.R;
import edu.cnm.deepdive.gallery.adapter.GalleryAdapter;
import edu.cnm.deepdive.gallery.databinding.FragmentGalleryBinding;
import edu.cnm.deepdive.gallery.model.Image;
import edu.cnm.deepdive.gallery.viewmodel.MainViewModel;
import java.util.List;

public class GalleryFragment extends Fragment {

  private static final int ADD_IMAGE_REQUEST = 1023;

  private FragmentGalleryBinding binding;
  private MainViewModel viewModel;
  private GalleryAdapter adapter;

  @Override
  public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentGalleryBinding.inflate(inflater, container, false);
    Context context = getContext();
    //noinspection ConstantConditions
    int span = (int) Math.floor(context.getResources().getDisplayMetrics().widthPixels
        / context.getResources().getDimension(R.dimen.gallery_item_width));
    binding.galleryView.setLayoutManager(new GridLayoutManager(context, span));
    adapter = new GalleryAdapter(context);
    binding.galleryView.setAdapter(adapter);
    binding.addImage.setOnClickListener((v) -> {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(
          Intent.createChooser(intent, getString(R.string.select_image_prompt)), ADD_IMAGE_REQUEST);
    });
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
    viewModel.getImages().observe(getViewLifecycleOwner(), this::updateGallery);
    viewModel.getImage().observe(getViewLifecycleOwner(), this::updateGallery);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_gallery, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    boolean handled = true;
    //noinspection SwitchStatementWithTooFewBranches
    switch (item.getItemId()) {
      case R.id.action_refresh:
        viewModel.loadImages();
        break;
      default:
        handled = super.onOptionsItemSelected(item);
    }
    return handled;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ADD_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
      OpenUploadProperties action = NavGraphDirections.openUploadProperties(data.getData());
      //noinspection ConstantConditions
      Navigation.findNavController(getView()).navigate(action);
    }
  }

  private void updateGallery(List<Image> images) {
    adapter.getImages().clear();
    adapter.getImages().addAll(images);
    adapter.notifyDataSetChanged();
  }

  private void updateGallery(Image image) {
    List<Image> images = adapter.getImages();
    if (image != null && !images.contains(image)) {
      images.add(0, image);
      adapter.notifyItemInserted(0);
    }
  }

}