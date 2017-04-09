package tmnt.example.rxjavademo3;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.Arrays;
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
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Person p1 = new Person("tmnt", 20);
    Person p2 = new Person("duke", 21);
    Person p3 = new Person("tony", 22);

    Car mCar1 = new Car("benz");
    Car mCar2 = new Car("bmw");
    Car mCar3 = new Car("audi");


    Person[] ps = {p1, p2, p3};
    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p1.setCars(new Car[]{mCar1, mCar2, mCar3});
        p2.setCars(new Car[]{mCar1, mCar3});
        p3.setCars(new Car[]{mCar2, mCar3});

        flatmap();

    }

    /**
     * create操作符创建observable
     */

    public void create() {
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
    }

    /**
     * just/from操作符创建observable
     */
    public void just() {
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
    }

    /**
     * AsyncSubject
     * 发射订阅前最后一个元素 仅一个
     */
    public void async() {
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
    }

    /**
     * BehaviorSubject
     * 发射订阅前最后一个及以后的元素
     */
    public void behavior() {
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
    }

    /**
     * PublishSubject
     * 发射订阅后的元素
     */
    public void pubish() {
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
    }

    /**
     * map操作符
     * 对单个元素转换
     */
    public void map() {
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
    }

    /**
     * flatMap操作符
     * 对多个元素进行转化
     */
    public void flatmap() {
        Observable.just(1)
                .flatMap(integer -> Observable.fromArray(ps[integer].getCars()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Log.i(TAG, "flatmap: " + s.getName()));

    }

    /**
     * interval操作符
     * 发送整数序列(或以一定时间间隔)
     */
    public void interval() {
        Observable<Long> o = Observable.interval(2, TimeUnit.SECONDS);
        o.subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long value) {
                Log.i(TAG, "onNext: " + String.valueOf(value));
                if (value >= 10) {

                    disposable.dispose();//停止接收
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * timer操作符
     * 一定时间间隔发射一个元素
     */
    public void timer() {
        Observable.timer(2, TimeUnit.SECONDS).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long value) {
                Log.i(TAG, "timer: " + String.valueOf(value));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * groupBy操作符
     * 将元素分类
     */
    public void groupBy() {
        Observable.range(0, 10).groupBy(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return integer % 2 == 0 ? "偶数" : "奇数";
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GroupedObservable<String, Integer>>() {
                    @Override
                    public void accept(final GroupedObservable<String, Integer> stringIntegerGroupedObservable) throws Exception {
                        Log.i(TAG, "group: " + stringIntegerGroupedObservable.getKey());
                        stringIntegerGroupedObservable.subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                Log.i(TAG, "key: " + stringIntegerGroupedObservable.getKey() + " value:" + integer);
                            }
                        });
                    }
                });
    }


}
