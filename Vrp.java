import java.util.ArrayList;
import java.util.Random;


public class Vrp {
	
	static int distanceMatrixDim1=0;
	static int distanceMatrixDim2=0;
	static int totalRoutes=10;
	static int constCapacity=50;
	static int mySeed=999;
	static int totalCustomers=30; 

	
	public static void main(String[] args) 
	{
		//create the depot
		Node depot = new Node();
		depot.x = 50;
		depot.y = 50;
		
		//Create the list with the customers
		Random ran = new Random (mySeed); 
		ArrayList <Node> customers = new ArrayList<Node>();
		for (int i = 1 ; i <= totalCustomers; i++)
		{
			Node cust = new Node();
			cust.x = ran.nextInt(100);
			cust.y = ran.nextInt(100);
			cust.demand = 4 + ran.nextInt(7);
			cust.ID = i;
			customers.add(cust);
		}			
		//Build the allNodes array and the corresponding distance matrix
		ArrayList <Node> allNodes = new ArrayList<Node>();
		allNodes.add(depot);
		for (int i = 0 ; i < customers.size(); i++)
		{
			Node cust = customers.get(i);
			allNodes.add(cust);
		}
		for (int i = 0 ; i < allNodes.size(); i++)
		{
			Node nd = allNodes.get(i);
			nd.ID = i;
		}
		// This is a 2-D array which will hold the distances between node pairs
		// The [i][j] element of this array is the distance required for moving 
		// from the i-th node of allNodes (node with id : i)
		// to the j-th node of allNodes list (node with id : j)
		double [][] distanceMatrix = new double [allNodes.size()][allNodes.size()];
		for (int i = 0 ; i < allNodes.size(); i++)
		{
			Node from = allNodes.get(i);
			for (int j = 0 ; j < allNodes.size(); j++)
			{
				Node to = allNodes.get(j);
				double Delta_x = (from.x - to.x);
				double Delta_y = (from.y - to.y);
				double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));
				distance = Math.round(distance);
				distanceMatrix[i][j] = distance;
			}
		}
		//initial greedy solution
		Solution solution=new Solution(totalRoutes,constCapacity);
		makeSolutionGreedy(depot, customers,distanceMatrix, solution);
		double initialCost=solution.cost;
		System.out.println("Initial solution greedy:");
		printSolution(solution);
		System.out.println("*******************");
		//apply local search to it
		makeSolutionLocalSearch(distanceMatrix, solution);
		System.out.println("*******************");
		System.out.println("After local Search");
		printSolution(solution);
		System.out.println("*******************");
		System.out.println("Cost saved:"+(initialCost-solution.cost));
	}
	

	
	static void makeSolutionGreedy(Node argDepot, ArrayList <Node> argCustomers,double [][] argDistMatrix,
									Solution argSolution){
		// Let route be the route contained in s
		for (int i = 0 ; i < argCustomers.size(); i++)
			argCustomers.get(i).isRouted = false;

		int totalCustomersInserted=0;
		Route route;
		boolean currentRouteOpen;
		for(int k = 0; (k< totalRoutes)&& (totalCustomersInserted<totalCustomers) ; k++)
		{
			route = argSolution.routes[k];
			ArrayList <Node> nodeSequence = route.nodes;
			nodeSequence.add(argDepot);
			currentRouteOpen=true;
			//repeat until insert all customers or current route should be closed
			for (int i = 0 ; (i < argCustomers.size()&& currentRouteOpen); i++)
			{
				int positionOfTheNextOne = -1;
				double bestCostForTheNextOne = Double.MAX_VALUE;
				//This is the last customer of the route (or the depot if the route is empty)
				Node lastInTheRoute = nodeSequence.get(nodeSequence.size() - 1);
				//First Step: Identify the non-routed nearest neighbor (his position in the customers list) of the last node in the nodeSequence list
				for (int j = 0 ; j < argCustomers.size(); j++)
				{
					Node candidate = argCustomers.get(j);
					if ((candidate.isRouted == false)&& (candidate.demand+route.load <= route.capacity))
					{
						//This is the cost for moving from the last to the candidate one
						double trialCost = argDistMatrix[lastInTheRoute.ID][candidate.ID];
						//If this is the minimal cost found so far -> store this cost and the position of this best candidate
						if (trialCost < bestCostForTheNextOne)
						{
							positionOfTheNextOne = j;
							bestCostForTheNextOne = trialCost;
						}
					}
				}//customers inner
				
				if (positionOfTheNextOne != -1){
					//We have found the customer to be pushed!!! He is located in the positionOfTheNextOne.
					Node insertedNode = argCustomers.get(positionOfTheNextOne);
					nodeSequence.add(insertedNode);
					argSolution.cost = argSolution.cost + bestCostForTheNextOne;
					route.cost = route.cost + bestCostForTheNextOne;
					route.load = route.load + insertedNode.demand;
					insertedNode.isRouted = true;
					totalCustomersInserted++;
				}else{
					// could not find anyone to add to current route. add depot and close it
					currentRouteOpen=false;
					lastInTheRoute = nodeSequence.get(nodeSequence.size() - 1);
					nodeSequence.add(argDepot);
					argSolution.cost = argSolution.cost + argDistMatrix[lastInTheRoute.ID][argDepot.ID];
					route.cost = route.cost + argDistMatrix[lastInTheRoute.ID][argDepot.ID];
				} //customers inner
			}//customers (insertions) outer
		}//routes

	}
	
	static void printCustomers(ArrayList <Node> argCust)
	{
		for (int i = 0 ; i < argCust.size(); i++)
		{
			Node cust = argCust.get(i);
			System.out.println("i"+i+":"+", demand:"+cust.demand+", routed:"+cust.isRouted);
		}
	}
	
	static void printSolution(Solution argSolution)
	{
		System.out.println("Total Solution Cost:"+argSolution.cost);
		System.out.println("-------------------");
		Route vehicle;
		for(int i=0;i<totalRoutes;i++){
			vehicle=argSolution.routes[i];
			
			ArrayList<Node> nodes = vehicle.nodes;
			
			if (nodes.size() > 1)
			{
				System.out.println("Route:"+i+", load:"+vehicle.load+", cost:"+vehicle.cost);
				System.out.println("Nodes(id,demand):");
				for(int j = 0; j< nodes.size(); j++)
				{
					if (j == nodes.size()-1)
						System.out.print("(id:"+nodes.get(j).ID +",d:"  + nodes.get(j).demand + ")");
					else
						System.out.print("(id:"+nodes.get(j).ID +",d:"  + nodes.get(j).demand + "),");
				}
				System.out.println();
			}
			else{
				System.out.println("Route:"+i+", load:"+vehicle.load+", cost:"+vehicle.cost+ " (Empty route)");
			}
			System.out.println("-------------------");
		}
	}
	
	static void findBestRelocationMove(RelocationMove rm, Route rt, double [][] distanceMatrix) 
	{
		//This is a variable that will hold the cost of the best relocation move
		double bestMoveCost = Double.MAX_VALUE;

		//We will iterate through all available nodes to be relocated
		for (int relIndex = 1; relIndex < rt.nodes.size() - 1; relIndex++)
		{
			//Node A is the predecessor of B
			Node A = rt.nodes.get(relIndex - 1);
			//Node B is the relocated node
			Node B = rt.nodes.get(relIndex);
			//Node C is the successor of B
			Node C = rt.nodes.get(relIndex + 1);

			//We will iterate through all possible re-insertion positions for B
			for (int afterInd = 0; afterInd < rt.nodes.size() -1; afterInd ++)
			{
				// Why do we have to write this line?
				// This line has to do with the nature of the 1-0 relocation
				// If afterInd == relIndex -> this would mean the solution remains unaffected
				// If afterInd == relIndex - 1 -> this would mean the solution remains unaffected
				if (afterInd != relIndex && afterInd != relIndex - 1)
				{
					//Node F the node after which B is going to be reinserted
					Node F = rt.nodes.get(afterInd);
					//Node G the successor of F
					Node G = rt.nodes.get(afterInd + 1);

					//The arcs A-B, B-C, and F-G break
					double costRemoved1 = distanceMatrix[A.ID][B.ID] + distanceMatrix[B.ID][C.ID];
					double costRemoved2 = distanceMatrix[F.ID][G.ID];
					double costRemoved = costRemoved1 + costRemoved2;

					//The arcs A-C, F-B and B-G are created
					double costAdded1 = distanceMatrix[A.ID][C.ID];
					double costAdded2 = distanceMatrix[F.ID][B.ID] + distanceMatrix[B.ID][G.ID];
					double costAdded = costAdded1 + costAdded2;

					//This is the cost of the move, or in other words
					//the change that this move will cause if applied to the current solution
					double moveCost = costAdded - costRemoved;

					//If this move is the best found so far
					if (moveCost < bestMoveCost)
					{
						//set the best cost equal to the cost of this solution
						bestMoveCost = moveCost;

						//store its characteristics
						rm.positionOfRelocated = relIndex;
						rm.positionToBeInserted = afterInd;
						rm.moveCost = moveCost;
					}
				}
			}
		}
	}


	private static void applyRelocationMove(RelocationMove rm, Solution s, Route rt, double[][] distanceMatrix) 
	{
		//This is the node to be relocated
		Node B = rt.nodes.get(rm.positionOfRelocated);
		//Take out the relocated node
		rt.nodes.remove(rm.positionOfRelocated);
		//Reinsert the relocated node into the appropriate position
		//Where??? -> after the node that WAS (!!!!) located in the rm.positionToBeInserted of the route
		//Watch out!!! If the relocated customer is reinserted backwards we have to re-insert it in (rm.positionToBeInserted + 1)
		if (rm.positionToBeInserted < rm.positionOfRelocated)
			rt.nodes.add(rm.positionToBeInserted + 1, B);
		////else (if it is reinserted forward) we have to re-insert it in (rm.positionToBeInserted)
		else
			rt.nodes.add(rm.positionToBeInserted, B);
		//update the cost of the solution and the corresponding cost of the route object in the solution
		s.cost = s.cost + rm.moveCost;
		rt.cost = rt.cost + rm.moveCost;
	}


	
	
	static void makeSolutionLocalSearch(double [][] argDistMatrix,Solution argSolution){
		//this is a boolean flag (true/false) for terminating the local search procedure
		boolean terminationCondition;
		//this is a counter for holding the local search iterator
		int localSearchIterator = 0;
		//This is an object for holding the best relocation move that can be applied to the candidate solution
		RelocationMove rm = new RelocationMove();
		Route route;
		for(int k = 0; k< totalRoutes; k++) //for all routes
		{
			route = argSolution.routes[k];	
			terminationCondition = false;
			// Until the termination condition is set to true repeat the following block of code
			while (terminationCondition == false)
			{
				//Initialize the relocation move rm
				rm.positionOfRelocated = -1;
				rm.positionToBeInserted = -1;
				rm.moveCost = Double.MAX_VALUE;
				//With this function we look for the best relocation move
				//the characteristics of this move will be stored in the object rm
				findBestRelocationMove(rm, route, argDistMatrix);			
				// If rm (the identified best relocation move) is a cost improving move, or in other words
				// if the current solution is not a local optimum
				if (rm.moveCost < 0)
				{
					System.out.println("[Relocation!] Route:" +k 
					+" ,node id:" + route.nodes.get(rm.positionOfRelocated).ID
					+ ", positionOfRelocated:"+ rm.positionOfRelocated + 
					", positionToBeInserted:" + rm.positionToBeInserted
					+ ", cost: " + rm.moveCost);
					//This is a function applying the relocation move rm to the candidate solution
					applyRelocationMove(rm, argSolution, route, argDistMatrix);

				}
				else
				{
					//if no cost improving relocation move was found,
					//or in other words if the current solution is a local optimum
					//terminate the local search algorithm
					terminationCondition = true;
				}
				
				localSearchIterator = localSearchIterator + 1;
			}	
		}		
		System.out.println("Local search total iterations: " + localSearchIterator );
	}

}
/////////////////////
class Node 
{
	int x;
	int y;
	int ID;
	int demand;

	// true/false flag indicating if a customer has been inserted in the solution
	boolean isRouted; 

	Node(int argDemand) 
	{
		demand=argDemand;
	}
	Node()
	{
	}
}
/////////////////////
class Solution 
{
	double cost;
	Route routes[];

	//This is the Solution constructor. It is executed every time a new Solution object is created (new Solution)
	Solution (int argTotalRoutes,int argRouteCapacity)
	{
		// A new route object is created addressed by rt
		// The constructor of route is called
		routes = new Route[argTotalRoutes];
		for(int i=0;i<argTotalRoutes;i++)
			routes[i] = new Route(argRouteCapacity);
		cost = 0;
	}
}
/////////////////////
class Route 
{

	ArrayList <Node> nodes;
	double cost;
	int capacity;
	int load;
	//This is the Route constructor. It is executed every time a new Route object is created (new Route)
	Route(int argCapacity) 
	{
		cost = 0;
		load=0;
		capacity=argCapacity;
		// A new arraylist of nodes is created
		nodes = new ArrayList<Node>();
	}
}
//////////
class RelocationMove 
{
	int positionOfRelocated;
	int positionToBeInserted;
	double moveCost;

	RelocationMove() 
	{
	}
}