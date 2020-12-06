package com.abubusoft.powertrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abubusoft.kripton.android.Logger;
import com.abubusoft.powertrainer.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * https://developers.google.com/identity/sign-in/android/sign-in
 * https://firebase.google.com/docs/auth/android/google-signin?authuser=0
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

  private GoogleSignInClient mGoogleSignInClient;
  private FragmentLoginBinding binding;

  public LoginFragment() {
    // Required empty public constructor
  }

  /**
   * https://medium.com/droid-log/androidx-activity-result-apis-the-new-way-7cfc949a803c
   */
  ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
          result -> {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            handleSignInResult(task);
          });

  @Override
  public void onStart() {
    super.onStart();

    // [START on_start_sign_in]
    // Check for existing Google Sign In account, if the user is already signed in
    // the GoogleSignInAccount will be non-null.
    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
    updateUI(account);
    // [END on_start_sign_in]
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentLoginBinding.inflate(inflater);

    // Button listeners
    binding.signInButton.setOnClickListener(this);
    binding.signOutButton.setOnClickListener(this);
    binding.signOutAndDisconnect.setOnClickListener(this);

    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();

    // Build a GoogleSignInClient with the options specified by gso.
    mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

    // Set the dimensions of the sign-in button.
    binding.signInButton.setSize(SignInButton.SIZE_STANDARD);
    binding.signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

    return binding.getRoot();
  }

  private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);

      // Signed in successfully, show authenticated UI.
      updateUI(account);
    } catch (ApiException e) {
      // The ApiException status code indicates the detailed failure reason.
      // Please refer to the GoogleSignInStatusCodes class reference for more information.
      Logger.info("signInResult:failed code=" + e.getStatusCode());
      updateUI(null);
    }
  }

  private void signIn() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();

    resultLauncher.launch(signInIntent);
  }

  private void signOut() {
    mGoogleSignInClient.signOut()
            .addOnCompleteListener(requireActivity(), task -> {
              updateUI(null);
            });
  }
  // [END signOut]

  // [START revokeAccess]
  private void revokeAccess() {
    mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(requireActivity(), task -> {
              updateUI(null);
            });
  }
  // [END revokeAccess]

  private void updateUI(@Nullable GoogleSignInAccount account) {
    if (account != null) {
      binding.status.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));

      binding.signInButton.setVisibility(View.GONE);
      binding.signOutAndDisconnect.setVisibility(View.VISIBLE);
    } else {
      binding.status.setText(R.string.signed_out);

      binding.signInButton.setVisibility(View.VISIBLE);
      binding.signOutAndDisconnect.setVisibility(View.GONE);
    }
  }

  @Override
  public void onClick(View v) {
    long id = v.getId();
    if (id == R.id.sign_in_button) {
      signIn();
    } else if (id == R.id.sign_out_button) {
      signOut();
    } else if (id == R.id.sign_out_and_disconnect) {
      revokeAccess();
    }

  }
}