package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.*;

public class EmployeeHelper {

    public static String makeEmployeeTree() {
        Employee employee = Employee.find(1); // root employee
        List<Employee> employees = Employee.all();
        Map<Long, List<Employee>> employeeMap = new HashMap<>();
        employeeMap.put(employee.getEmployeeId(), new LinkedList<>());
        employees.remove(employee);
        for (Employee e: employees)  {
            if (!employeeMap.containsKey(e.getReportsTo())) {
                employeeMap.put(e.getReportsTo(), new LinkedList<>());
            }
            employeeMap.get(e.getReportsTo()).add(e);
        }
        // and use this data structure to maintain reference information needed to build the tree structure

        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        String list = "<li><a href='/employees" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";
        if (employeeMap.containsKey(employee.getEmployeeId())) {
            List<Employee> reports = employeeMap.get(employee.getEmployeeId());
            for (Employee report : reports) {
                list += makeTree(report, employeeMap);
            }
        }
        return list + "</ul></li>";
    }
}
