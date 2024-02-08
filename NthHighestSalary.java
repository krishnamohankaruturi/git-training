package org.example.java8practice.java8programs;

import java.util.*;
import java.util.stream.Collectors;

public class NthHighestSalary {
    public static void main(String[] args) {
        //System.out.println(NthHighestSalary.getNthHighestSalary(3));
        NthHighestSalary.getNthHighestSalary(4);
    }
        public static  void  getNthHighestSalary(int n) {
            Map<String, Integer> map = new HashMap<>();
            map.put("anil", 10000);
            map.put("hari", 20000);
            map.put("ahmed", 15000);
            map.put("shreya", 17000);
            map.put("krishna", 16000);
            map.put("ramesh", 16000);
            map.put("satish", 10000);
            map.put("varun", 15000);
            //Map.Entry<String, Integer> stringIntegerEntry = map.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toList()).get(n-1);
            //return  stringIntegerEntry;

            Map.Entry<Integer, List<String>> nthHighest = map.entrySet().stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
                    .entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                    .collect(Collectors.toList())
                    .get(n - 1);
            System.out.println(nthHighest);

        }
}
