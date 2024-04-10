# ECSE422 Communication Network Designer

## Contributors
Mohammed Elsayed, 261053266 <br/>
Ayman Elsayed, 261053265 <br/>
Roxanne Archambault, 261052406 <br/>

## Requirements
Java 8 or higher <br/>
Terminal/Command line or IDE to compile and run the program. <br/>

## How to run program
In src folder  <br/> 
run command : javac Network.java </br>
run command : java Network ["path/filename"] [cost] <br/>

Program takes 2 arguments <br/>
First argument: input file with the correct format <br/>
Second argument: maximum cost of network <br/>

# Example of running the program
Cost constraint : 75 <br/>
<b> java Network "../input.txt" 75 </b> <br/>


# Description of program
Program is designed to create a network with all-to-all reliability.

From input text, the program initialize a network graph with N number of vertices and N(N-1)/2 edges.

### Method 1: Exhaustive Enumeration
All possible network design choices are generated. The reliability and cost of each choice are calculated. The network with the best reliability, that does not exceed the cost limit, is chosen to be our maximized reliability.

### Method 2: Kruskal's Algorithm
After parsing the edges, the edges are sorted based on cost. A spanning tree is produced using kruskal algorithm with the sorted edges.
If the reliability of spanning tree can still be maximized without exceeding the cost limit, the spanning tree adds an edge with the lowest cost. The reliability and cost is recalculated with the edge in the network graph. If the reliability can further be maximized, keep adding additional edges until no more edges can be added. The reliability is calculated using exhaustive method.

When relibaility is maximized, the network outputs a design with the number of edges and the link between each link for each method

## Output

4-city with cost limit 50 <br/>
![4-city](/Images/4-city.png)

5-city with cost limit 60 <br/>
![5-city](/Images/5-city.png)

6-city with cost limit 65 <br/>
![6-city](/Images/6-city.png)









