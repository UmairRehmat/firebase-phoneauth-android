package com.urtechhub.firebaseauthworking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.urtechhub.firebaseauthworking.databinding.ActivityMainBinding;
import com.urtechhub.firebaseauthworking.databinding.LoginDialogLayoutBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity
        extends AppCompatActivity
{
    private LoginDialogLayoutBinding dialogBinding;
    private ActivityMainBinding mBinding;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.login.setOnClickListener(v -> openDialogForTheNumber());
        mBinding.logout.setOnClickListener(v -> {
            AuthApp.instance()
                   .getmAuth()
                   .signOut();
            mBinding.logout.setVisibility(View.GONE);
            mBinding.login.setVisibility(View.VISIBLE);
        });
        if (AuthApp.instance()
                   .getmAuth()
                   .getCurrentUser() != null)
        {
            mBinding.logout.setVisibility(View.VISIBLE);
            mBinding.login.setVisibility(View.GONE);
        }
    }

    private void openDialogForTheNumber()
    {
        final Dialog loginDialog = new Dialog(this, R.style.CustomTransparentDialog);
        dialogBinding = DataBindingUtil.inflate(
                LayoutInflater.from(this), R.layout.login_dialog_layout, null,
                false);
        loginDialog.setContentView(dialogBinding.getRoot());
        loginDialog.show();
        loginDialog.setCancelable(false);
        dialogBinding.dismis.setOnClickListener(v -> loginDialog.dismiss());

        dialogBinding.sendOpt.setOnClickListener(v -> {
            String number = dialogBinding.number.getText()
                                                .toString()
                                                .trim();
            if (TextUtils.isEmpty(number) || !number.startsWith("3") || number.length() != 10)
            {
                dialogBinding.number.setError(getString(R.string.please_enter_valid_number));
                dialogBinding.number.requestFocus();
                return;
            }

            AuthApp.instance()
                   .getmPhoneAuthProvider()
                   .verifyPhoneNumber("+92" + number, 60,
                                      TimeUnit.SECONDS, this, mCallBacks);
            dialogBinding.sendOpt.setVisibility(View.GONE);
            dialogBinding.progressCircular.setVisibility(View.VISIBLE);
        });
        dialogBinding.verifyOtp.setOnClickListener(v -> {
            String otp = dialogBinding.opt.getText()
                                          .toString()
                                          .trim();
            if (TextUtils.isEmpty(otp))
            {
                dialogBinding.opt.setError(getString(R.string.please_enter_valid_number));
                dialogBinding.opt.requestFocus();
                return;
            }
            dialogBinding.progressCircular.setVisibility(View.VISIBLE);
            dialogBinding.verifyOtp.setVisibility(View.GONE);
            verifyPhoneNumberWithCode(mVerificationId, otp);
        });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code)
    {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        AuthApp.instance()
               .getmAuth()
               .signInWithCredential(credential)
               .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
               {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task)
                   {
                       if (task.isSuccessful())
                       {

                           FirebaseUser user = task.getResult()
                                                   .getUser();
                           Toast.makeText(MainActivity.this, user.getPhoneNumber(),
                                          Toast.LENGTH_SHORT)
                                .show();
                           mBinding.login.setVisibility(View.GONE);
                           mBinding.logout.setVisibility(View.VISIBLE);
                           dialogBinding.dismis.performClick();
                       }
                       else
                       {
                           if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                           {
                               dialogBinding.opt.setError("Invalid code.");
                               dialogBinding.opt.requestFocus();
                               dialogBinding.progressCircular.setVisibility(View.GONE);
                               dialogBinding.verifyOtp.setVisibility(View.VISIBLE);
                           }
                       }
                   }
               });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
        {
            Toast.makeText(MainActivity.this, "verified", Toast.LENGTH_SHORT)
                 .show();
            dialogBinding.dismis.performClick();
            signInWithPhoneAuthCredential(phoneAuthCredential);

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e)
        {
            if (e instanceof FirebaseAuthInvalidCredentialsException)
            {
                Toast.makeText(MainActivity.this, "Invalid Request", Toast.LENGTH_SHORT)
                     .show();
            }
            else if (e instanceof FirebaseTooManyRequestsException)
            {
                Toast.makeText(MainActivity.this, "sms qouta over", Toast.LENGTH_SHORT)
                     .show();
            }
            dialogBinding.progressCircular.setVisibility(View.GONE);
            dialogBinding.sendOpt.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                 .show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken)
        {
            dialogBinding.numberContainer.setVisibility(View.GONE);
            dialogBinding.otpContainer.setVisibility(View.VISIBLE);
            dialogBinding.progressCircular.setVisibility(View.GONE);
            dialogBinding.verifyOtp.setVisibility(View.VISIBLE);
            mVerificationId = s;
//            mResendToken = forceResendingToken;
            super.onCodeSent(s, forceResendingToken);
        }
    };
}
