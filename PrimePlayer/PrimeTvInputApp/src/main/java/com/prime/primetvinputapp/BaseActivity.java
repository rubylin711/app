package com.prime.primetvinputapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.utils.TVMessage;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private PrimeDtvServiceInterface gPrimeDtvServiceInterface;
    private static List<BookInfo> gFakeBookList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gPrimeDtvServiceInterface = PrimeTvInputAppApplication.get_prime_dtv_service();
    }

    public void onMessage(TVMessage msg) {

    }
}
