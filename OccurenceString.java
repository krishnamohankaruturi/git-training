package org.example.java8practice.java8programs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OccurenceString {
    public static void main(String[] args) {
        String input ="ilovejavatechie";
        //String[] results = input.split("");
        Map<String, Long> map = Arrays.stream(input.split("")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println(map);// Occurences of each String
         // Find the duplicates
        Map<String, Long> duplicateMap = Arrays.stream(input.split("")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<String> duplicateVals = duplicateMap.entrySet().stream().filter(val -> val.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        System.out.println(duplicateVals);
        //first non repeating character
        Map<String, Long> nonRepeatingMap = Arrays.stream(input.split("")).collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        Map.Entry<String, Long> nonRepeatEntry = nonRepeatingMap.entrySet().stream().filter(val -> val.getValue() == 1).findFirst().get();
        System.out.println(nonRepeatEntry);
        System.out.println("///////////////////////////////////////////////////////////////////");
        int[] numbers ={5,9,11,2,8,21,1};
        Integer secondHighestNum = Arrays.stream(numbers).boxed().sorted(Comparator.reverseOrder()).skip(1).findFirst().get();
        System.out.println(secondHighestNum);
        System.out.println("///////////////////////////////////////////////////////////////////");
        String[] strArray = {"java","techie","spring boot", "microservices"};
        String s = Arrays.stream(strArray).reduce((word1, word2) -> word1.length() > word2.length() ? word1 : word2).get();
        System.out.println(s);
        System.out.println("///////////////////////////////////////////////////////////////////");
        int[] nums ={5,9,11,2,8,21,1};
        List<String> collect = Arrays.stream(nums).boxed()
                .map(s1 -> s1 + "")
                .filter(s2 -> s2.startsWith("1"))
                .collect(Collectors.toList());
        System.out.println(collect);

    }
}
