package com.helospark.mycraft.mycraft.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameItems {

	@Autowired
	MyIdGenerator idGenerator;

	List<List<Integer>> betterLists = new ArrayList<>();

	public GameItems() {

	}

	Map<Integer, GameItem> items = new HashMap<Integer, GameItem>();

	public void addGameItem(GameItem gameItem) {
		items.put(gameItem.getId(), gameItem);
	}

	public GameItem getById(int id) {
		return items.get(id);
	}

	public boolean isItemBetterThan(int item1, int item2) {
		for (int i = 0; i < betterLists.size(); ++i) {
			int firstIndex = betterLists.get(i).indexOf(item1);
			int secondIndex = betterLists.get(i).indexOf(item2);
			if (firstIndex != -1 && secondIndex != -1
					&& firstIndex > secondIndex) {
				return true;
			}
		}
		return false;
	}

	public void addItemBetterList(List<Integer> betterList) {
		betterLists.add(betterList);
	}
}
