# CSCI 440 - Database Systems

This is the base upstream repository for CSCI 440: Database Systems  

It contains the homework assignments, as well as the class project information.

## Getting Your Private Copy

Please use the following steps to create a *private* copy of this repo for your work:

- Create a *private* repository in your own account by
    - Going to <https://github.com/new>
    - Enter the name `csci-440-fall2022-private`
    - Select `Private`
    - Navigate to the `Settings` -> `Manage Access` section
    - Add `1cg` as a collaborator
Once your repository is initialized, you can pull it down to your local machine:

```bash
$ git clone <your github repo url>
```

You can find the github repo url when you look at the repository in github.

Next, you should add the class repository as an upstream git repo:

```bash
$ cd csci-440-fall2022-private
$ git remote add upstream https://github.com/msu/csci-440-fall2022.git
$ git pull upstream main
$ git push origin main
```
This will synchronize your private repository with the class repository.

When you want to get an update from the public class repository you can run this command:

```
$ git pull upstream main
``` 

## Homeworks

Homeworks can be found in the `src/test/java/edu/montana/csci/csci440/homework` directory and are given in the form of
Tests.

## Project: Chinook Web App

You will be implementing a web application against a SQL store, found in `/db/chinook.db`

The web application will be an open-ended project, but must include the following:

* All entities in the chinookdb must be surfaced in the web application
* The Employee entity must have all CRUD operations surfaced
* A tree UI of the Employee hierarchy
* Track search with at least two inputs must be implemented
* At least one report-like page must exist that makes use of a `GROUP BY` clause
* Implement at least one Redis cache

### Project Rubric

The project will be largely graded via automated tests.  The grading will be broken down along the following lines:

* 74.9% - The automated test suite (if the test suite passes, you are guaranteed to get a C on the project)
* 15% - A recorded demo, demonstrating the following functionality
  * Navigating the core entities in the database
  * CRUD operations on the Employee entity
  * Track search
  * Paging implemented in the Track main view 
  * A Group By based report page
* 10% - Code quality and satisfaction of the Redis cache, determined by manual inspection
* .1% - At least one use of htmx (https://htmx.org) in the application to add dynamic AJAX behavior

### Project Resources

* [Spark Java Docs](http://sparkjava.com/documentation)
* [Velocity Templates Docs](https://velocity.apache.org/engine/2.2/user-guide.html#loops)
* [Active JDBC Docs](https://javalite.io/activejdbc)
