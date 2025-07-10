package BMG.banManagerGUI.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements InventoryHolder {
    private final String guiType;
    private final String guiTitle;
    
    public GUIHolder(String guiType, String guiTitle) {
        this.guiType = guiType;
        this.guiTitle = guiTitle;
    }
    
    @Override
    public Inventory getInventory() {
        return null;
    }
    
    public String getGuiType() {
        return guiType;
    }
    
    public String getGuiTitle() {
        return guiTitle;
    }
}