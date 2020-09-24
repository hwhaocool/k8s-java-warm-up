package com.yellow.k8s.warmup;

import java.util.stream.IntStream;

/**
 * @author YellowTail
 * @since 2020-09-23
 */
public class InfiniteStream {

    public static void main(String[] args) {

//        IntStream.of(1, 2, 3, 4)
//                .filter(e -> e > 2)
//                .peek(e -> System.out.println("Filtered value: " + e))
//                .map(e -> e * e)
//                .peek(e -> System.out.println("Mapped value: " + e))
//                .sum();

        System.out.println("-----");
        IntStream intStream = IntStream.iterate(0, k -> k + 1 > 9 ? 0 : k + 1)
                .limit(30);


        IntStream peek = intStream.peek(e -> System.out.println("xxxx value: " + e));

        peek.forEach(k -> System.out.println(" >> " + k));
    }

}
