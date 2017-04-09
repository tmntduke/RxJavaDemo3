package tmnt.example.rxjavademo3;

import java.util.List;

/**
 * Created by tmnt on 2017/2/24.
 */
public class Person {

    private String name;
    private int age;
    private Car[] mCars;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Car[] getCars() {
        return mCars;
    }

    public void setCars(Car[] cars) {
        mCars = cars;
    }
}
