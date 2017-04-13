package com.helospark.mycraft.mycraft.actor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.window.MessageHandler;

public class InventoryComponent extends ActorComponent {

    public static final String INVENTORY_COMPONENT_NAME = "InventoryComponent";

    public static final int MAX_AMOUNT_PER_ITEM = 64;

    List<InventoryItem> itemsOnHand = new ArrayList<InventoryItem>();
    List<InventoryItem> itemsOnBackpack = new ArrayList<InventoryItem>();
    int maxHandItems;
    int maxBackpackItems;
    MessageHandler messager;
    int activeElementInInventory;
    InventoryItem defaultItem;
    GameItems gameItems;

    public InventoryComponent(int maxHandItems, int maxBackpackItems, InventoryItem defaultItem) {
        super(INVENTORY_COMPONENT_NAME);
        ApplicationContext context = Singleton.getInstance().getContext();
        this.maxHandItems = maxHandItems;
        this.maxBackpackItems = maxBackpackItems;
        this.defaultItem = defaultItem;
        messager = context.getBean(MessageHandler.class);
        gameItems = context.getBean(GameItems.class);
        for (int i = 0; i < maxHandItems; ++i) {
            itemsOnHand.add(defaultItem);
        }
        for (int i = 0; i < maxBackpackItems; ++i) {
            itemsOnBackpack.add(defaultItem);
        }

    }

    public boolean addToInventory(InventoryItem inventoryItem) {
        boolean wasAdded = tryToAddList(itemsOnHand, inventoryItem);

        if (!wasAdded) {
            wasAdded = tryToAddList(itemsOnBackpack, inventoryItem);
        }
        return wasAdded;
    }

    public InventoryItem getCurrentInventoryItem() {
        // TODO: only works for objects one after another
        if (activeElementInInventory < itemsOnHand.size() && activeElementInInventory >= 0) {
            return itemsOnHand.get(activeElementInInventory);
        } else {
            return defaultItem;
        }
    }

    private boolean tryToAddList(List<InventoryItem> list, InventoryItem item) {
        for (int i = 0; i < list.size(); ++i) {
            InventoryItem itemOnHand = list.get(i);
            if (itemOnHand == defaultItem
                    || (itemOnHand.equals(item) && canIncreaseAmountBy(itemOnHand, item.getAmount()))) {
                if (itemOnHand == defaultItem) {
                    list.set(i, item);
                } else {
                    itemOnHand.increaseAmountBy(item.getAmount());
                }
                return true;
            }
        }
        return false;
    }

    private boolean canIncreaseAmountBy(InventoryItem itemOnHand, int amount) {
        if (itemOnHand.getAmount() + amount > MAX_AMOUNT_PER_ITEM) {
            return false;
        }
        if (!gameItems.getById(itemOnHand.getId()).isStackable()) {
            return false;
        }
        return true;
    }

    @Override
    public Object createFromXML(Element node) {

        return null;
    }

    @Override
    public void afterInit() {

    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public ActorComponent createNew() {
        return new InventoryComponent(this.maxHandItems, this.maxBackpackItems, defaultItem);
    }

    public List<InventoryItem> getHandItemList() {
        return itemsOnHand;
    }

    public List<InventoryItem> getInventoryElementList() {
        return itemsOnBackpack;
    }

    public void changeActiveComponentWith(int amount) {
        activeElementInInventory -= amount;
        if (activeElementInInventory >= maxHandItems) {
            activeElementInInventory = 0;
        }
        if (activeElementInInventory < 0) {
            activeElementInInventory = maxHandItems - 1;
        }
        System.out.println(activeElementInInventory + " " + amount);
    }

    public void decreaseCurrentItem() {
        itemsOnHand.get(activeElementInInventory).decreaseAmount();
        if (itemsOnHand.get(activeElementInInventory).getAmount() <= 0) {
            itemsOnHand.set(activeElementInInventory, defaultItem);
        }
    }

    public boolean isHandEmptyAt(int i) {
        if (i >= 0 && i < itemsOnHand.size()) {
            return itemsOnHand.get(i) == defaultItem;
        }
        return true;
    }

    public boolean isInventoryEmptyAt(int i) {
        if (i >= 0 && i < itemsOnBackpack.size()) {
            return itemsOnBackpack.get(i) == defaultItem;
        }
        return true;
    }

    public GameItem getCurrentGameItem() {
        return gameItems.getById(getCurrentInventoryItem().getId());
    }

    public int getCurrentGameItemIndex() {
        return activeElementInInventory;
    }

    public InventoryItem getDefaultItem() {
        return defaultItem;
    }
}
