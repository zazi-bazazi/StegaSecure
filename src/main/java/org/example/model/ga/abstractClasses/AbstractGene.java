package org.example.model.ga.abstractClasses;

public abstract class AbstractGene<T> {
    protected T value;

    public T getValue() { return this.value; }
    public void setValue(T value) { this.value = value; }

    public AbstractGene(T value, Object... params) {this.value = value;};
//    public abstract IGene<T> generateGeneRand(Object... params);
//    public abstract IGene<T> generateGeneMath(Object... params);
}
