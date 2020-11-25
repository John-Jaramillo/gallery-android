package edu.cnm.deepdive.gallery.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.Transformations;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.gallery.model.Image;
import edu.cnm.deepdive.gallery.model.User;
import edu.cnm.deepdive.gallery.service.ImageRepository;
import edu.cnm.deepdive.gallery.service.UserRepository;
import io.reactivex.disposables.CompositeDisposable;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel implements LifecycleObserver {

  private final UserRepository userRepository;
  private final ImageRepository imageRepository;
  private final MutableLiveData<GoogleSignInAccount> account;
  private final MutableLiveData<User> user;
  private final MutableLiveData<List<Image>> images;
  private final MutableLiveData<Image> image;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  public MainViewModel(
      @NonNull @NotNull Application application) {
    super(application);
    userRepository = new UserRepository(application);
    imageRepository = new ImageRepository(application);
    account = new MutableLiveData<>(userRepository.getAccount());
    user = new MutableLiveData<>();
    images = new MutableLiveData<>();
    image = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
//    getUserProfile();
    loadImages();
  }

  public LiveData<User> getUser() {
    return user;
  }

  public LiveData<List<Image>> getImages() {
    return images;
  }

  public LiveData<Image> getImage() {
    return image;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void signOut() {
    throwable.setValue(null);
    pending.add(
        userRepository.signOut()
            .subscribe(
                () -> user.postValue(null),
                throwable::postValue
            )
    );
  }

  public void storeImage(Uri uri, String title, String description) {
    throwable.setValue(null);
    pending.add(
        imageRepository.add(uri, title, description)
            .subscribe(
                image::postValue,
                throwable::postValue
            )
    );
  }

  public void resetStoredImage() {
    image.setValue(null);
  }

  public void loadImages() {
    throwable.setValue(null);
    pending.add(
        imageRepository.getAll()
            .subscribe(
                images::postValue,
                throwable::postValue
            )
    );
  }

  @OnLifecycleEvent(Event.ON_STOP)
  private void clearPending() {
    pending.clear();
  }

  private void getUserProfile() {
    pending.add(
        userRepository.getServerUserProfile()
            .subscribe(
                user::postValue,
                throwable::postValue
            )
    );
  }

}
