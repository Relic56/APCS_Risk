import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import main.Player;
import territoryMap.TerritoryMap;
/*
 * Implements a not very intelligent AI.
 */
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
	 * unless no hostile armies are around. Priority = number of enemy troops around - number of troops deployed on territory
	 * 2. Territory next to territory needed to complete continent: Priority = 10
	 * TODO: Implement above condition
	 * 3. Frontier territory with the least armies deployed currently
	 * 4. Frontier territory with most neutral territories around it.
	 * 5. Other territory (should never happen)
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
					priority += TerritoryMap.getNumArmiesDeployedOn(neighbors);
				}
				priority-=TerritoryMap.getNumArmiesDeployedOn(s);
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
	/*
	 * Chooses a territory to attack and attacks it. Much like deploying reinforcements, it depends on the
	 * priority assigned to each territory.
	 * AI is rather aggressive and will choose to attack lightly defended player territories more often than neutral ones.
	 * Priority assigned as follows:
	 * 1. Territory needed to complete continent set - assigned priority of 10. 
	 * 2. Neutral Territory: Assigned priority of 10-(surrounding number of neighbors)
	 * 3. Territory belonging to other player: 15 - number of armies on it
	 */
	public void attack()
	{
		Map<Integer, String> attackPriority = new TreeMap<Integer,String>();
		HashSet<String> occ = (HashSet<String>) getOccupiedTerritories();
		
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
