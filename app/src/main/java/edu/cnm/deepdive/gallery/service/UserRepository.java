package edu.cnm.deepdive.gallery.service;

import android.content.Context;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import edu.cnm.deepdive.gallery.model.User;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class UserRepository {

  private final Context context;
  private final GoogleSignInService signInService;
  private final GalleryWebService webService;

  public UserRepository(Context context) {
    this.context = context;
    signInService = GoogleSignInService.getInstance();
    webService = GalleryWebService.getInstance();
  }

  public GoogleSignInAccount getAccount() {
    return signInService.getAccount();
  }

  public Single<User> getServerUserProfile() {
    return signInService.refresh()
        .observeOn(Schedulers.io())
        .flatMap((account) -> webService.getProfile(getBearerToken(account.getIdToken())));
  }

  public Completable signOut() {
    return signInService.signOut()
        .observeOn(Schedulers.io());
  }

  private String getBearerToken(String idToken) {
    return String.format("Bearer %s", idToken);
  }

}
