package no.mesan.handterminator.model;

/**
 *@author Joakim Rishaug
 * The class represents the structurs of a drawer in the left-side Navigation Drawer.
 */
public class Drawer {

    int iconId;
    String title;

    public Drawer(int iconId, String title){
        this.iconId = iconId;
        this.title = title;
    }

    public int getIconId() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }
}
