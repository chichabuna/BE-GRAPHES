package org.insa.algo.shortestpath;
import org.insa.graph.*;
import java.util.*;

import org.insa.algo.AbstractInputData;
import org.insa.algo.AbstractSolution.Status;
import org.insa.algo.utils.*;

public class AStarAlgorithm extends DijkstraAlgorithm {
	
	public float sommetvisite2;
    public AStarAlgorithm(ShortestPathData data) {
        super(data);
        this.sommetvisite2=0;
    }
    @Override
    public ShortestPathSolution doRun() {
    	
        ShortestPathData data = getInputData();
        ShortestPathSolution solution = null;
       
        Graph graph = data.getGraph();
        int size_graph = graph.size();
        
        BinaryHeap<Label> bin_heap = new BinaryHeap<Label>();
        List<LabelStar> list_label = new ArrayList<LabelStar>(size_graph);
        for(int i =0; i < size_graph; i++)
        {
        	list_label.add(null);
        }
        
        Node node_origin = data.getOrigin();
        Node node_destination = data.getDestination();
        
        //insertion du premier label
        LabelStar label_origin = new LabelStar(node_origin.getId(),true,0,null,0);
        list_label.set(node_origin.getId(), label_origin);
        bin_heap.insert(label_origin);
        notifyOriginProcessed(node_origin);
        
        boolean arrive = false;
        
        //algorithme
        while (bin_heap.isEmpty()==false && arrive == false)
        {
        	//on trouve le plus petit element du tas
        	LabelStar x = (LabelStar) bin_heap.deleteMin();
        	Node node_x = graph.get(x.getSommet());	
        	
        	//cet element devient "marqué"
        	x.marque = true;
        	list_label.set(node_x.getId(), x);
        	notifyNodeMarked(node_x);
        	if (node_x == node_destination)
        	{
        		arrive = true;
        	}
        	
        	//cout de cette element
        	double cost_x = x.getCost();
        	
        	List<Arc> successors = node_x.getSuccessors();

        	// pour chacun de ses successeurs
        	for (Arc arc_successor : successors)
        	{
        		Node node_y = arc_successor.getDestination();
        		LabelStar label_y = list_label.get(node_y.getId());
        		double cout_est_y=0;
        		
        		if (label_y == null)
        		{
        			notifyNodeReached(node_y);
        			// on calcul le cout estim� en fonction de la nature du cout d�sir�e
        			
        			// en longueur
        			if (data.getMode() == AbstractInputData.Mode.LENGTH) {
        				cout_est_y= node_y.getPoint().distanceTo(node_destination.getPoint());
        			}
        			// en temps
        			else {
        				int vitessemax = 37;   // 37 m par seconde
        				cout_est_y= node_y.getPoint().distanceTo(node_destination.getPoint())/vitessemax;
        			}
        			
        			LabelStar init_label_y = new LabelStar(node_y.getId(),false,1e10,null,cout_est_y);
        			list_label.set(node_y.getId(), init_label_y);
        			label_y = init_label_y;
        			this.sommetvisite2++;
        			
        		}
        		
        		//si un successeur n'est pas marqué
        		if (label_y.marque == false)
        		{
        			double cost_y = label_y.getCost();
        			if (cost_y > (cost_x + data.getCost(arc_successor)))
					{
        				double new_cost = cost_x + data.getCost(arc_successor);
        				cout_est_y = label_y.cout_est;
						LabelStar new_label_y = new LabelStar(node_y.getId(),false,new_cost,arc_successor,cout_est_y);
						new_label_y.inHeap = label_y.inHeap;
						if (new_label_y.inHeap==true)
						{
							bin_heap.remove(label_y);
						}
						new_label_y.inHeap = true;
						list_label.set(node_y.getId(), new_label_y);
						bin_heap.insert(new_label_y);
						
					}
        		}
        	}
        }
        
        // Destination has no predecessor, the solution is infeasible...
        if (arrive == false) {
            solution = new ShortestPathSolution(data, Status.INFEASIBLE);
        }
        else {

            // The destination has been found, notify the observers.
            notifyDestinationReached(node_destination);

            // Create the path from the array of predecessors...
         
            ArrayList<Arc> arcs = new ArrayList<>();
            Arc arc = list_label.get(node_destination.getId()).getArcPere();
            Node node_pere = null;
            
            
            while (arc != null) {
                arcs.add(arc);
                node_pere = arc.getOrigin();
                arc = list_label.get(node_pere.getId()).getArcPere();
            }

            // Reverse the path...
            Collections.reverse(arcs);

            // Create the final solution.
            solution = new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, arcs));
        }
        return solution;
    }
}
