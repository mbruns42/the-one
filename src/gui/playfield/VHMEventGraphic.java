package gui.playfield;

import input.VHMEvent;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Graphics class for VHMEvents
 *
 * Created by Marius Meyer on 17.02.17.
 */
public class VHMEventGraphic extends PlayFieldGraphic {

    /**
     * Color of the circle around the event representing {@link VHMEvent#eventRange}
     */
    private static Color eventRangeColor = Color.lightGray;

    /**
     * Color of the circle around the event representing {@link VHMEvent#safeRange}
     */
    private static Color safeRangeColor = Color.orange;

    /**
     * Color of the circle around the event representing {@link VHMEvent#maxRange}
     */
    private static Color maxRangeColor = Color.yellow;

    /**
     * Color of the event's location. {@link VHMEvent#location}
     */
    private static Color eventLocationColor = Color.red;

    /**
     * Color of the event's name {@link VHMEvent#name}
     */
    private static Color eventNameColor = Color.black;


    /**
     * Boolean that defines, if all ranges should be drawn.
     * If false, only the event range will be drawn.
     */
    private static boolean drawAllRanges = true;

    /**
     * If true, the event's name will be drawn.
     */
    private static boolean drawEventName = true;

    /**
     * The event this graphics class is representing
     */
    private VHMEvent event;

    /**
     * Creates a new event graphics
     * @param e the event this graphics object will represent
     */
    public VHMEventGraphic(VHMEvent e){
        this.event = e;
    }

    @Override
    public void draw(Graphics2D g2) {
        drawEvent(g2);
    }

    /**
     * Draws a range for an event.
     *
     * @param g2 The graphics to draw on
     * @param range The range around the event, that should be drawn
     * @param c The color this range should be drawn with
     */
    private void drawEventRange(Graphics2D g2, double range, Color c){
        Ellipse2D.Double eventRange = new Ellipse2D.Double(scale(event.getLocation().getX()-range),
                scale(event.getLocation().getY()-range), scale(range * 2),
                scale(range * 2));

        g2.setColor(c);
        g2.draw(eventRange);
    }

    /**
     * Draws the event
     * @param g2 The graphics to draw on
     */
    private void drawEvent(Graphics2D g2){

        //only draw other ranges, when enabled
        if (drawAllRanges){
            drawEventRange(g2,event.getMaxRange(),maxRangeColor);
            drawEventRange(g2,event.getSafeRange(),safeRangeColor);
        }

        drawEventRange(g2,event.getEventRange(),eventRangeColor);


        if (drawEventName) {
            g2.setColor(eventNameColor);
            // Draw event's name next to it
            g2.drawString(event.getName(), scale(event.getLocation().getX()),
                    scale(event.getLocation().getY()));
        }

		/* draw node rectangle */
        g2.setColor(eventLocationColor);
        g2.drawRect(scale(event.getLocation().getX()-1),scale(event.getLocation().getY()-1),
                scale(2),scale(2));

    }

    /**
     * Sets if  all event ranges should be drawn
     * @param draw if true, all ranges are drawn
     */
    public static void setDrawAllRanges(boolean draw){
        drawAllRanges = draw;
    }

    /**
     * Sets if the event names should be drawn
     * @param draw if true, the event names are drawn
     */
    public static void setDrawEventName(boolean draw){
        drawEventName = draw;
    }

    /**
     * Checks, if a event graphics is equal to anoter one by comparing the {@link VHMEvent#id}
     * of the events they are representing.
     *
     * @param o the object to compare to
     * @return true, if the objects are representing a event with the same id
     */
    @Override
    public boolean equals(Object o){
        if (o instanceof VHMEventGraphic) {
            return ((VHMEventGraphic) o).event.getID() == this.event.getID();
        }else return false;
    }
}
