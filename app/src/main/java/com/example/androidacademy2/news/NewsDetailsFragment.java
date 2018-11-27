package com.example.androidacademy2.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.androidacademy2.AboutActivity;
import com.example.androidacademy2.AppDatabase;
import com.example.androidacademy2.DB.NewsEntity;
import com.example.androidacademy2.Intro.IntroFragment;
import com.example.androidacademy2.MainActivity;
import com.example.androidacademy2.R;

import java.util.concurrent.Callable;

public class NewsDetailsFragment extends Fragment {


    static public String url;
    WebView webView;
    private ImageView image;
    private TextView titleText, fullText, publisheDate;
    private static final String LOG = "My_Log";
    private static final String ARGS_URL = "url";
    private AppDatabase db;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Context context;
    private NewsFragmentListener listener;
    private Button but_edit;
    private Button but_del;


    static public NewsDetailsFragment newInstance(String url) {
        NewsDetailsFragment pageFragment = new NewsDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGS_URL, url);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NewsFragmentListener) {
            listener = (NewsFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_news_details, container, false);
        context = getContext();

        if (getArguments() != null) {
            url = getArguments().getString(ARGS_URL);
        }
        Log.d(LOG, url);

        db = AppDatabase.getAppDatabase(context);

        titleText = view.findViewById(R.id.title_news_details);
        fullText = view.findViewById(R.id.full_news_details);
        publisheDate = view.findViewById(R.id.date_news_details);
        image = view.findViewById(R.id.image_news_details);
        but_edit = view.findViewById(R.id.button_edit);
        but_del = view.findViewById(R.id.button_delete);


        but_edit.setOnClickListener(v -> {
            listener.onNewsEditClicked(url);
        });


        but_del.setOnClickListener(v -> {
            Disposable disposable1 = deleteNews()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
            compositeDisposable.add(disposable1);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                getFragmentManager().popBackStack();
            } else {
                listener.deleteFragmentDetails();
            }
        });
        /*webView = findViewById (R.id.web_news);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);*/
        MainActivity.f = 1;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        Disposable disposable2 = db.newsDao().findById(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showNewsDetails, this::logError);
        compositeDisposable.add(disposable2);
    }

    private void showNewsDetails(NewsEntity news) {


        titleText.setText(news.getTitle());
        fullText.setText(news.getPreviewText());
        publisheDate.setText(news.getPublishDate());
        Glide.with(context).load(news.getImageUrl()).into(image);
        //setTitle(news.getCategory());
    }

    private void logError(Throwable th) {
        Log.d(LOG, "" + th);
    }

    @Override
    public void onStop() {
        super.onStop();
        // compositeDisposable.dispose();
    }

    public Completable deleteNews() {
        return Completable.fromCallable((Callable<Void>) () -> {
            db.newsDao().deleteById(url);
            Log.d(LOG, "1 news delete");
            return null;
        });
    }
}
