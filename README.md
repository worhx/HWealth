# HWealth - Mobile App

## Introduction
The scope of this project is to develop a Mobile app that tracks patient BMI, steps and nutritions.

## Set up
1. Install Android Studio


## Conventions

#### Imports / Exports
* Remove unused imports and reorder imports
* Depending on IDE, `Ctrl + Alt + O` to reorganize imports for Intellij

#### Naming
* Give meaningful naming to `variables`, `classes`, `methods`
* All variables should be in camelcase (E.g. reviewDate, actionBy)
* For more java naming conventions, [see Here.](https://www.geeksforgeeks.org/java-naming-conventions)

#### Git
* Always perform `Git pull` to fetch latest changes before commencing work
* Prefixed Ticket Number should be in `ALL CAPS`
* Commits:
    * Format: `[<<Issue#>>][<<Prefix>>-<<Ticket#>>] Describe changes for that commit`
    * `Issue#:` Refer to github
    * Types of prefixes:
        * `FEATURE` for feature commit
        * `TECH` for tech task commit
        * `BUGFIX` for bug commit
        * `MISC` for misc commit. Use when unsure which prefix to tag
    * Example of commit message:
        * `[#10][BUGFIX-004] Some short description regarding that bug`

* Branching:
    * `master` for each sprint's MVP release
    * `development` merged from individual branch
    * `<<Prefix>>/<<Ticket#>>` for working branch regarding a specific task (See Prefix)

* Merging:
    * Perform `Git Pull` and resolve conflicts first
    * Follow commit convention
    * Perform `Git Push` to your individual branch
    * Create `Pull Request` and assign other team member for review
