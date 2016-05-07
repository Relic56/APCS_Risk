import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AIPlayer extends Player
{
	public AIPlayer(String startingLocation)
	{
		super("CPU", startingLocation);
	}
	
	/*
	 * Chooses a territory to deploy to. Frontier territories have higher priority. 
	 * Priority is as follows:
	 * 1. Frontier territory with highest amount of enemy troops around it. Neutral territories are generally ignored
	 * unless no hostile armies are around
	 * 2. Frontier territory with the least armies deployed currently
	 * 3. Frontier territory with most neutral territories around it.
	 * 4. Other territory (should never happen)
	 * 
	 * If multiple territories are assigned the same priority the AI will split them evenly
	 */
	public void deploy()
	{
		HashSet<String> occ = (HashSet<String>) getOccupiedTerritories();
		int reinforcements = calculateReinforcements();
		Map<Integer,Set<String>> priorities = new TreeMap<Integer,Set<String>>();
		//Priority = number of hostile armies or number of neutral territories if no hostile armies around
		for(String s : occ)
		{
			int priority = 0;
			//Get number of hostile armies surrounding this
			if(getHostileNeighbors(s).isEmpty())
			{
				priority += getNeutralNeighbors(s).size();
			}
			else
			{
				for(String neighbors : getHostileNeighbors(s))
				{
					priority += TerritoryMap.getNumArmiesDeployedOn(s);
				}
			}
			if(priorities.get(new Integer(priority))==null)
			{
				Set<String> set = new HashSet<String>();
				set.add(s);
				priorities.put(new Integer(priority), set);
			}
			else
			{
				Set<String> set = priorities.get(new Integer(priority));
				set.add(s);
				priorities.put(new Integer(priority),set);
			}
		}
		//Choose territories with highest priority
		
		Set<String> highestPriority = priorities.get(priorities.keySet().iterator().next());//Finds the first value in priority set
		int numPerTerritory = reinforcements/highestPriority.size();
		int extra = reinforcements % highestPriority.size();
		for(String s : highestPriority)
		{
			deployReinforcements(s,numPerTerritory);
			if(extra!=0)
			{
				deployReinforcements(s,1);
				extra--;
			}
		}
	}
	
	public void attack()
	{
		
	}
	public void reinforce()
	{
		
	}
	
	public void takeTurn()
	{
		deploy();
		attack();
		reinforce();
		
	}
}
