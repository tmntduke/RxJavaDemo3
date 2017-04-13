package tmnt.example.rxjavademo3;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
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

    Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p1.setCars(new Car[]{mCar1, mCar2, mCar3});
        p2.setCars(new Car[]{mCar1, mCar3});
        p3.setCars(new Car[]{mCar2, mCar3});

        combineLatest();

    }

    /**
     * create操作符创建observable
     */

    private void create() {
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
    private void just() {
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
    private void async() {
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
    private void behavior() {
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
    private void pubish() {
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
    private void map() {
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
    private void flatmap() {
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
    private void interval() {
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
    private void timer() {
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
    private void groupBy() {
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

    /**
     * 结合两个Observable
     * 数量为少的Observable
     */
    private void zip() {
        Observable<Integer> observable = Observable.just(1, 2, 3);
        Observable<String> observable1 = Observable.just("a", "b", "c", "d");
        Observable.zip(observable, observable1, ((integer, s) -> s + integer))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                        Log.i(TAG, "onNext: " + value);
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
     * 模拟被压
     * filter过滤减少数量
     */
    private void filter() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        }).filter(integer -> integer % 10 == 0)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.i(TAG, "filter: " + integer));

    }

    /**
     * 模拟被压
     * sample为两秒采样 减少数量
     */
    private void sample() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        }).sample(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.i(TAG, "sample: " + integer));
    }

    /**
     * 模拟被压
     * 减缓时间
     */
    private void reduce() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                    Thread.sleep(2000);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.i(TAG, "reduce: " + integer));
    }

    /**
     * 将数据按指定个数整合为集合
     * 有不同参数
     */
    private void buffer() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .buffer(3)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integers -> {
                    for (int i = 0; i < integers.size(); i++) {
                        Log.i(TAG, "buffer: " + integers + "  " + integers.get(i));
                    }
                });
    }

    /**
     * 和buffer相似，最终合成的为Observable
     * 有不同参数
     */
    private void window() {
        Observable.just(1, 2, 3, 4, 5, 6)
                .window(3)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integerObservable ->
                        integerObservable.subscribe(integer ->
                                Log.i(TAG, "window: " + integer)));
    }

    /**
     * 将数据应用到指定的函数
     * （之后产生的结果再次与之后的数据应用）
     */
    private void scan() {
        Observable.just(2, 3, 4, 5)
                .scan((integer, integer2) -> integer + integer2)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.i(TAG, "scan: " + integer));
    }

    /**
     * 过滤重复
     */
    private void distinct() {
        Observable.just(1, 2, 2, 3, 3, 4)
                .distinct()
                .subscribe(integer -> Log.i(TAG, "distinct: " + integer));
    }

    private void flowable() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0; i <= 100; i++) {
                    e.onNext(i);
                }
            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(5);
                        subscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.i(TAG, "onNext: " + integer);
                        //subscription.request(3);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 并行发射多个observable
     */
    private void concat() {
        Observable<Integer> observable = Observable.just(1);
        Observable<Integer> observable1 = Observable.just(2);
        Observable<Integer> observable2 = Observable.just(3);

        Observable.concat(observable, observable1, observable2)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.i(TAG, "concat: " + integer));

    }

    /**
     * 使用函数将最近的两个observable的数据结合起来
     */
    private void combineLatest() {
        Observable<Integer> observable = Observable.just(1, 2, 3, 4);
        Observable<String> observable1 = Observable.just("a", "b", "c");
        Observable.combineLatest(observable, observable1, ((integer, s) -> integer + s))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Log.i(TAG, "combineLatest: " + s));

    }


}

