package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.*;

public class EmployeeHelper {

    public static String makeEmployeeTree() {
        // TODO, change this to use a single query operation to get all employees
        Employee employee = Employee.find(1); // root employee
        List<Employee> employees = Employee.all();
        Map<Long, List<Employee>> employeeMap = new HashMap<>();
        employeeMap.put(employee.getEmployeeId(), new LinkedList<>());
        employees.remove(employee);
        for (Employee e: employees)  {
            if (employeeMap.containsKey(e.getReportsTo())) {
                employeeMap.get(e.getReportsTo()).add(e);
            } else {
                employeeMap.put(e.getReportsTo(), new LinkedList<>());
                employeeMap.get(e.getReportsTo()).add(e);
            }
        }
        // and use this data structure to maintain reference information needed to build the tree structure

        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just uses the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
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
