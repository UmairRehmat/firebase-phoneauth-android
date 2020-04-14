package com.urtechhub.firebaseauthworking;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;

import androidx.annotation.NonNull;

public class AuthApp
        extends Application
{
    @SuppressLint("StaticFieldLeak") private static Context mContext;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider mPhoneAuthProvider;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        mPhoneAuthProvider = PhoneAuthProvider.getInstance();
    }

    //*********************************************************************
    public static @NonNull
    AuthApp instance()
    //*********************************************************************
    {
        return (AuthApp)mContext;
    }

    public FirebaseAuth getmAuth()
    {
        return mAuth;
    }

    public void setmAuth(FirebaseAuth mAuth)
    {
        this.mAuth = mAuth;
    }

    public PhoneAuthProvider getmPhoneAuthProvider()
    {
        return mPhoneAuthProvider;
    }

    public void setmPhoneAuthProvider(PhoneAuthProvider mPhoneAuthProvider)
    {
        this.mPhoneAuthProvider = mPhoneAuthProvider;
    }
}
