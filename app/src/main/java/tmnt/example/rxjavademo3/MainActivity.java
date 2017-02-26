package tmnt.example.rxjavademo3;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Person p1 = new Person("tmnt", 20);
    Person p2 = new Person("duke", 21);
    Person p3 = new Person("tony", 22);
    Person[] ps = {p1, p2, p3};
    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Observable observable = Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();

                Log.i(TAG, "subscribe: " + Thread.currentThread().getName());

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());

        observable.subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                Log.i(TAG, "accept: " + o);
                Log.i(TAG, "accept: " + Thread.currentThread().getName());
            }
        });

        Observable.just(1, 2, 3).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, "just: " + integer);
            }
        });

        int[] num = {1, 2, 3};
        Observable.fromArray(num).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<int[]>() {
                    @Override
                    public void accept(int[] ints) throws Exception {
                        for (int i = 0; i < ints.length; i++) {
                            Log.i(TAG, "from: " + ints[i]);
                        }
                    }
                });

        AsyncSubject<Integer> asyncSubject = AsyncSubject.create();
        asyncSubject.onNext(1);
        asyncSubject.onNext(2);
        asyncSubject.onNext(3);
        asyncSubject.onComplete();
        asyncSubject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, "async: " + integer);
            }
        });

        BehaviorSubject<Integer> behaviorSubject = BehaviorSubject.create();
        behaviorSubject.onNext(1);
        behaviorSubject.onNext(2);
        behaviorSubject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, "behavior: " + integer);
            }
        });
        behaviorSubject.onNext(3);
        behaviorSubject.onNext(4);

        PublishSubject<Integer> publishSubject = PublishSubject.create();
        publishSubject.onNext(1);
        publishSubject.onNext(2);
        publishSubject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, "publish: " + integer);
            }
        });

        publishSubject.onNext(3);

        Observable.just(0, 1, 2).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Integer, Person>() {
                    @Override
                    public Person apply(Integer integer) throws Exception {
                        return ps[integer];
                    }
                }).subscribe(new Consumer<Person>() {
            @Override
            public void accept(Person s) throws Exception {
                Log.i(TAG, "map: " + s.getName());
            }
        });


        Observable.just(0, 1, 2).flatMap(new Function<Integer, ObservableSource<Person>>() {
            @Override
            public ObservableSource<Person> apply(Integer integer) throws Exception {
                return Observable.just(ps[integer]);
            }
        }).subscribe(new Consumer<Person>() {
            @Override
            public void accept(Person s) throws Exception {
                Log.i(TAG, "flat: " + s.getName());
            }
        });


        Observable<Long> o = Observable.interval(2, TimeUnit.SECONDS);
        o.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long value) {
                Log.i(TAG, "onNext: "+String.valueOf(value));
               if (value>=10){

                   disposable.dispose();
               }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        Observable.timer(2,TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long value) {
                Log.i(TAG, "timer: "+String.valueOf(value));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
