package org.zhenchao.dora.spi.ext4;

/**
 * @author zhenchao.wang 2017-12-30 14:52
 * @version 1.0.0
 */
public class Element {

    private String name;

    private int age;

    public Element() {
    }

    public Element(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public Element setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Element setAge(int age) {
        this.age = age;
        return this;
    }
}
