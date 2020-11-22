package edu.cnm.deepdive.gallery.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.gallery.model.User;
import edu.cnm.deepdive.gallery.service.UserRepository;
import io.reactivex.disposables.CompositeDisposable;
import org.jetbrains.annotations.NotNull;

public class MainViewModel extends AndroidViewModel implements LifecycleObserver {

  private final UserRepository userRepository;
  private final MutableLiveData<GoogleSignInAccount> account;
  private final MutableLiveData<User> user;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  public MainViewModel(
      @NonNull @NotNull Application application) {
    super(application);
    userRepository = new UserRepository(application);
    account = new MutableLiveData<>(userRepository.getAccount());
    user = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    getUserProfile();
  }

  public LiveData<User> getUser() {
    return user;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void signOut() {
    pending.add(
        userRepository.signOut()
            .subscribe(
                () -> user.postValue(null),
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
