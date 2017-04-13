package com.helospark.mycraft.mycraft.xml;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.actor.GameItems;
import com.helospark.mycraft.mycraft.helpers.XmlHelpers;

@Service
public class ItemBetterListLoader implements XmlLoader {

	@Autowired
	GameItems gameItems;

	@Override
	public void parseXml(Element rootElement) {
		List<Integer> loadedList = XmlHelpers.parseIntegerList(rootElement,
				"item");
		gameItems.addItemBetterList(loadedList);
	}

}
