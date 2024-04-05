# ECSE422 Communication Network Designer

## Requirements
Java  <br/>
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
After parsing the edges, the edges are sorted based on either reliability or cost. A spanning tree is produced using kruskal algorithm with the sorted edges.
If the reliability of spanning tree doesn't meet the goals, the spanning tree adds an edge with the lowest cost. The reliability and cost is recalculated with the edge in the network graph. If the goal isn't obtained, keep adding additional edges until no more edge can be added. The reliability is calculated using exhaustive method.

When goal is met, the network outputs a design with the number of edges and the link between each link.

## Limitation of program
Program is quick and can calculate up to 18-20 edges before slowing down when applying exhaustive method to find reliability of additional edges.





