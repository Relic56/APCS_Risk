
package main;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import achievement.Achievement;
import achievement.AchievementManager;
import battle.BattleHandler;
import battle.BattleResults;
import card.Card;
import territoryMap.Territory;
import territoryMap.TerritoryMap;

import java.util.ArrayList;

/*
 * This program defines a player of the game.
 * Author: Ethan Yao
 */
public class Player
{
	private int numReinforcements;// the total number of reinforcements the
									// player can get
	private int numReinforcementsAvailable;// how many more armies the player
											// can deploy on this phase.
	private ArrayList<Card> deck;
	private Set<String> occupiedTerritories;//which locations the player has
	private ArrayList<Achievement> notFulfilled;//achievements that the player has not yet obtained
	private int setsTraded;

	private String name;

	/**
	 * Default constructor. Takes the player's name and starting territory.
	 * Starts off with 3 reinforcements per turn
	 * 
	 * @param name:
	 *            The name of the player
	 * @param starter:
	 *            The territory the player initially control.
	 */
	public Player(String name, String starter)
	{
		setsTraded = 0;
		numReinforcements = 3;
		occupiedTerritories = new HashSet<String>();
		occupiedTerritories.add(starter);
		TerritoryMap.get(starter).setOccupier(this);
		this.name = name;

		deck = new ArrayList<Card>();
		notFulfilled = new ArrayList<Achievement>(AchievementManager.achievements);
	}

	public int checkAchievements()
	{
		int bonus = 0;
		for(int i = 0; i < notFulfilled.size(); i++)
		{
			if(notFulfilled.get(i).isFullfilled(this))
			{
				bonus += notFulfilled.get(i).getBonus();
				System.out.println(notFulfilled.get(i).achieved());
				notFulfilled.remove(i);
				i--;
			}

		}
		return bonus;
	}

	/**
	 * Mounts an attack from a territory to another.
	 * 
	 * @param attacker:
	 *            The territory that the attack is being mounted from.
	 * @param other:
	 *            The territory being invaded.
	 * @param armies:
	 *            The number of armies being used for the attack. If this number
	 *            is zero, an all out attack is mounted
	 * @return Returns result of battle.
	 */
	public BattleResults attackTerritory(String attackerID, String defenderID, int armies)
	{
		Territory defender = TerritoryMap.get(defenderID);

		BattleResults results = BattleHandler.doFullBattle(attackerID, defenderID, armies);
		if(results.getAttackSuccess())
		{
			if(defender.isOccupiedByPlayer()) // make sure territory is owned by some player
			{
				defender.getOccupier().disownTerritory(defenderID);
				defender.disownOccupier();
			}
			defender.setOccupier(this);
			occupiedTerritories.add(defender.getID());
		}

		return results;
	}

	/**
	 * Gets the number of reinforcements available on a new turn according to
	 * the following rules: Divide the number of territories by 3 and round
	 * down. Must be less than seven. Also calculates the bonuses based on
	 * territories owned. Returns number for AI purposes
	 *
	 */
	public int calculateReinforcements()
	{
		if(occupiedTerritories.size() < 9) // Less than 9 armies means only 3
											// armies a turn
		{
			numReinforcements = 3;
		}
		else
		{
			numReinforcements = occupiedTerritories.size() / 3;
		}

		// Calculates based on the number of sets owned.
		numReinforcements += TerritoryMap.calculateArmyBonusFromContinents(occupiedTerritories);
		numReinforcements += checkAchievements();
		//Super Hax Mode: For use with Achievement testing only. Comment out if playing actual game :)
		//numReinforcements += 100;

		numReinforcementsAvailable = numReinforcements;
		return numReinforcementsAvailable;
	}

	/*
	 * Deploys reinforcements onto the selected territory.
	 */
	public boolean deployReinforcements(String t, int amount)
	{
		if(!occupiedTerritories.contains(t))
		{
			return false;
		}
		if(amount > numReinforcementsAvailable)
		{
			return false;
		}

		TerritoryMap.get(t).incrementArmiesBy(amount);
		numReinforcementsAvailable -= amount;
		return true;
	}

	// Returns all territories that can be attacked by Player p
	public Set<String> getAttackTargets()
	{
		Set<String> returnSet = new HashSet<String>();
		for(String s : occupiedTerritories)
		{
			if(TerritoryMap.getNumArmiesDeployedOn(s) < 2)
				continue;

			Territory t = TerritoryMap.get(s);
			Set<String> neighbors = t.getAdjacentTerritories();
			for(String k : neighbors)
			{
				if(!occupiedTerritories.contains(k))
				{
					returnSet.add(k);
				}
			}
		}
		return returnSet;
	}

	public void disownTerritory(String t)
	{
		occupiedTerritories.remove(t);
	}

	public String getName()
	{
		return name;
	}

	public int getTotalNumReinforcements()
	{
		return numReinforcements;
	}

	public int getNumReinforcementsAvailable()
	{
		return numReinforcementsAvailable;
	}

	public boolean ownsTerritory(String t)
	{
		return occupiedTerritories.contains(t);
	}

	public boolean ownsContinent(String c)
	{
		return TerritoryMap.continentIsSubsetOfSet(c, occupiedTerritories);
	}

	// Returns true if selected territory neighbors a player owned territory
	// and if at least 1 neighbor territory has at least 2 armies
	public boolean isValidAttackTarget(String t)
	{
		return getAttackTargets().contains(t);
	}

	public Set<String> getOccupiedTerritories()
	{
		return occupiedTerritories;
	}

	public ArrayList<Card> getCards()
	{
		return deck;
	}

	public void addCards(Card c)
	{
		deck.add(c);
	}

	public boolean hasNoTerritories()
	{
		return occupiedTerritories.isEmpty();
	}

	public Set<String> getHostileNeighbors(String territory)
	{
		Set<String> returnSet = new HashSet<String>();
		for(String neighbor : TerritoryMap.get(territory).getAdjacentTerritories())
		{
			if(TerritoryMap.get(neighbor).getOccupier() != null
					|| !this.equals(TerritoryMap.getOccupierOnTerritory(neighbor)))
			{
				returnSet.add(neighbor);
			}
		}
		return returnSet;
	}
	public Set<String> getNeutralNeighbors(String territory)
	{
		Set<String> returnSet = new HashSet<String>();
		for(String neighbor : TerritoryMap.get(territory).getAdjacentTerritories())
		{
			if(!getHostileNeighbors(territory).contains(neighbor) && !this.ownsTerritory(neighbor))
			{
				returnSet.add(neighbor);
			}
		}
		return returnSet;
	}
	// Return true if player has at least 2 armies on at least 1 territory
	public boolean hasArmiesToAttackWith()
	{
		for(String terrID : occupiedTerritories)
		{
			if(TerritoryMap.getNumArmiesDeployedOn(terrID) > 1)
				return true;
		}
		return false;
	}

	public int getSetsTraded()
	{
		return setsTraded;
	}

	public void addReinforcements(int num)
	{
		numReinforcements += num;
	}

	public void incrementSets()
	{
		setsTraded++;
	}

	// returns all the owned territories that have at least one
	// adjacent territory with at least 1 army
	public Set<String> getFortifyTargets()
	{
		Set<String> targets = new HashSet<String>(occupiedTerritories);

		Iterator<String> iter = targets.iterator();
		while(iter.hasNext())
		{
			String terrID = iter.next();

			Iterator<String> neighborIter = TerritoryMap.get(terrID).getAdjacentTerritoriesOccupiedBy(this).iterator();
			while(neighborIter.hasNext())
			{
				String neighborTerr = neighborIter.next();
				int numRemoved = 0;
				if(TerritoryMap.getNumArmiesDeployedOn(neighborTerr) < 2)
				{
					// in case we need to remove more than 1 element, advance the iterator
					if(numRemoved != 0)
						iter.next();
					iter.remove();
					numRemoved ++;
				}
			}
		}

		return targets;
	}

	public boolean getCanFortify()
	{
		return getFortifyTargets().size() != 0;
	}

	public boolean canFortifyTerritory(String territoryToFortifyFromID)
	{
		return getFortifyTargets().contains(territoryToFortifyFromID);
	}

	public boolean hasCards()
	{
		return deck.size() != 0;
	}
	
	public void displayCards()
	{
		for(Card c : deck)
		{
			System.out.print("[Territory: " + c.getTerritory() + " Value: " + c.getValue() +"]");
		}
	}
}
