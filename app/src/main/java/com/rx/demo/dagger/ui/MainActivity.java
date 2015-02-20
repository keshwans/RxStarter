package com.rx.demo.dagger.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbuddha.daggerdemo.activitygraphs.R;
import com.rx.demo.dagger.dagger.Activity;
import com.rx.demo.dagger.dagger.DemoBaseActivity;
import com.rx.demo.dagger.model.User;
import com.rx.demo.dagger.rest.Github;
import com.rx.demo.dagger.util.MapToSingleUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.events.OnClickEvent;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import static rx.android.observables.ViewObservable.clicks;


public class MainActivity extends DemoBaseActivity {

    @Inject
    public Github api;
    @Activity
    @Inject
    Context context;

    private Observer<User> reloadFirstUserObserver;
    private Observer<User> reloadSecondUserObserver;
    private Observer<User> reloadThirdUserObserver;
    private Func1<OnClickEvent, Observable<ArrayList<User>>> clickToResponse;
    private Observable<ArrayList<User>> refreshAllObservable;
    private Observable<ArrayList<User>> usersObservable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestions_layout);
        ButterKnife.inject(this);

        initObservers();
        setupObservables();
        subscribeWithAllObservers(refreshAllObservable);
        subscribeWithAllObservers(usersObservable);
        setupCloseClicks();
    }



    private void setupObservables() {
        usersObservable = api.users().cache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());

        initClickToResponse();

        refreshAllObservable = clicks(findViewById(R.id.btnRefresh))
                                    .flatMap(clickToResponse);
    }

    private void initClickToResponse() {
        clickToResponse = new Func1<OnClickEvent, Observable<ArrayList<User>>>() {
            @Override
            public Observable<ArrayList<User>> call(OnClickEvent onClickEvent) {
                return usersObservable;
            }
        };
    }

    private void setupCloseClicks() {
        clicks(view(R.id.close1))
                .flatMap(clickToResponse)
                .map(new MapToSingleUser())
                .subscribe(reloadFirstUserObserver);

        clicks(view(R.id.close2))
                .flatMap(clickToResponse)
                .map(new MapToSingleUser())
                .subscribe(reloadSecondUserObserver);

        clicks(view(R.id.close3))
                .flatMap(clickToResponse)
                .map(new MapToSingleUser())
                .subscribe(reloadThirdUserObserver);
    }

    private void subscribeWithAllObservers(Observable<ArrayList<User>> observable) {
        ConnectableObservable<ArrayList<User>> connectableObservable = observable.publish();
        //get a single random user from the response and then have each of
        //the three screen elements subscribe to it thus updating the screens with new data
        connectableObservable.map(new MapToSingleUser()).subscribe(reloadFirstUserObserver);
        connectableObservable.map(new MapToSingleUser()).subscribe(reloadSecondUserObserver);
        connectableObservable.map(new MapToSingleUser()).subscribe(reloadThirdUserObserver);
        connectableObservable.connect();
    }

    private void initObservers() {
        reloadFirstUserObserver = new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(User user) {
                ((TextView)view(R.id.name1)).setText(user.login);
                Picasso.with(context)
                        .load(user.avatar_url)
                        .into((ImageView) view(R.id.avatar1));

            }
        };

        reloadSecondUserObserver = new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(User user) {
                ((TextView)view(R.id.name2)).setText(user.login);
                Picasso.with(context)
                        .load(user.avatar_url)
                        .into((ImageView) view(R.id.avatar2));
            }
        };

        reloadThirdUserObserver = new Observer<User>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(User user) {
                ((TextView)view(R.id.name3)).setText(user.login);
                Picasso.with(context)
                        .load(user.avatar_url)
                        .into((ImageView) view(R.id.avatar3));
            }
        };
    }

    public static int getRandomIndex(int size) {
        return new Random().nextInt(size);
    }
}
