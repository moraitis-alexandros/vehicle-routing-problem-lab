# vehicle-routing-problem-lab

<img width="619" height="403" alt="image" src="https://github.com/user-attachments/assets/02b852d1-23de-44df-a501-e1086699e01e" />


This repository contains a simple, dependency-free implementation of a heuristic approach to the Vehicle Routing Problem (VRP).

The project was created to accompany a related article for Optimization4All Community that explores how a VRP can be modeled, how the problem can be broken down into smaller components, and how appropriate data structures and heuristics can be used to find a practical solution.

The goal is not to provide a state-of-the-art or highly optimized VRP solver. Instead, the implementation focuses on keeping the code simple, readable, and easy to experiment with. By avoiding external libraries and keeping the implementation relatively straightforward, the algorithm can be easily adapted to specific requirements or extended with additional constraints and optimization techniques.

The implementation includes three heuristic approaches for constructing or improving VRP solutions:

* **Nearest Neighbour Heuristic**: builds routes by repeatedly selecting the closest unvisited customer.
* **Clarke & Wright Savings Algorithm**: constructs routes by combining routes based on the calculated savings.
* **Insertion Heuristic**: improves existing solutions by inserting customers into positions that minimize the additional routing cost.

The repository is intended primarily for learning, experimentation, and demonstrating the underlying ideas rather than competing with mature VRP libraries or production grade solvers.

Feel free to explore the code, experiment with it, and suggest improvements or alternative approaches.
