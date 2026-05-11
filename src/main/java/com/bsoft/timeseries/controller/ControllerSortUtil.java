package com.bsoft.timeseries.controller;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class ControllerSortUtil {

    /*
    List<String> sort contains <[-]field>*
     */
    public static List<Sort.Order> getSortOrder(List<String> sort) {

        List<Sort.Order> orders = new ArrayList<>();
        Sort.Order order = null;

        if (sort != null && sort.size() > 0) {
            for (String sortField : sort) {
                Sort.Direction direction = Sort.Direction.ASC;
                if (sortField.startsWith("-")) {
                    direction = Sort.Direction.DESC;
                    sortField = sortField.substring(1); // Remove the "-" prefix
                }
                // Use the direction enum directly
                order = new Sort.Order(direction, sortField);
                orders.add(order);
            }
        }
        return orders;
    }

}