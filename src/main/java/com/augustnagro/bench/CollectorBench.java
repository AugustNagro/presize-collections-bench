package com.augustnagro.bench;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Fork(2)
public class CollectorBench {


    /*
    We should see the most performance & allocation benefit
    for the presized toList collector with these sizes,
    given that not-presized ArrayLists grow every time a
    size of 10 * 1.5^i + 1 is reached.

    Values were generated with:

    IntStream.iterate(0, i -> i + 8)
        .map(i -> (int) (10 * Math.pow(1.5,i) + 1))
        .limit(5)
        .toArray()

     */
    @Param({"11", "257", "6569", "168342", "4314399"})
    public int size;

    public Random rand;

    @Setup(Level.Trial)
    public void setup() {
        rand = new Random();
    }

    @Benchmark
    public List<Integer> presized() {
        return rand.ints().limit(size).boxed().collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> presizedParallel() {
        return rand.ints().parallel().limit(size).boxed().collect(Collectors.toList());
    }

    @Benchmark
    public List<Integer> old() {
        Collector<Integer, ?, List<Integer>> slowCollector =
                Collector.of(ArrayList::new, List::add, (l, r) -> {
                    l.addAll(r);
                    return l;
                });

        return rand.ints().limit(size).boxed().collect(slowCollector);
    }

    @Benchmark
    public List<Integer> oldParallel() {
        Collector<Integer, ?, List<Integer>> slowCollector =
                Collector.of(ArrayList::new, List::add, (l, r) -> {
                    l.addAll(r);
                    return l;
                });

        return rand.ints().parallel().limit(size).boxed().collect(slowCollector);
    }
}
