package vrp.tools;

import vrp.entities.LocationNode;
import vrp.entities.Solution;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Swing-based GUI component ({@code JPanel}) that visually renders a VRP solution:
 * the depot, customer nodes, and connecting route lines, with a clickable
 * information box that shows details for a selected node.
 */
public class SolutionDesigner extends JPanel {

    private Solution solutionToDesign;

    private List<LocationNode> locationNodeList;

    // The dot that was clicked
    private LocationNode selectedLocationNode;

    // Whether to show the information box
    private boolean showCustomer = false;

    // Shared Random instance with fixed seed for consistent, different colors
    private static final Random colorRandom = new Random(12345);


    /**
     * Initializes the panel and registers a mouse listener. The listener detects
     * clicks near a location node's coordinates (within a small radius) and
     * marks that node as selected so its information can be displayed, then
     * triggers a repaint.
     */
    public SolutionDesigner() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (locationNodeList == null) {
                    return;
                }

                for (LocationNode locationNode : locationNodeList) {

                    int dx = e.getX() - locationNode.getX();
                    int dy = e.getY() - locationNode.getY();

                    // Check if the click is close to the dot
                    if (dx * dx + dy * dy <= 25) {

                        selectedLocationNode = locationNode;
                        showCustomer = true;

                        repaint();

                        break;
                    }
                }
            }
        });
    }


    /**
     * Stores the solution and location list to be rendered, then creates and
     * displays a new {@code JFrame} titled with the given panel label. The frame
     * hosts this panel, uses a fixed 1000x1000 preferred size, exits the
     * application on close, and is packed and centered before being shown.
     */
    public void drawSolution(Solution solutionToDesign, List<LocationNode> locationNodeList, String panelLabel) {

        this.locationNodeList = locationNodeList;
        this.solutionToDesign = solutionToDesign;

        JFrame frame = new JFrame("Algorithm: " + panelLabel);

        frame.add(this);

        this.setPreferredSize(new Dimension(1000, 1000));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }



    /**
     * Overridden Swing rendering method. Draws each location node as a filled
     * oval: blue and larger for the depot, red and smaller for customers. Draws
     * the routes by iterating over each route's node sequence and drawing
     * connecting lines between consecutive nodes, using a distinct random color
     * per route. If a node has been clicked/selected, draws a white information
     * box with a black border near that node showing its X coordinate, Y
     * coordinate, and demand value.
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);


        // -------------------------
        // Draw dots
        // -------------------------

        if (locationNodeList != null) {

            for (LocationNode locationNode : locationNodeList) {

                if (locationNode.isDepot()) {
                    g.setColor(Color.BLUE);

                    g.fillOval(
                            locationNode.getX() - 5,
                            locationNode.getY() - 5,
                            30,
                            30
                    );
                }
                else {
                    g.setColor(Color.RED);

                    g.fillOval(
                            locationNode.getX() - 5,
                            locationNode.getY() - 5,
                            10,
                            10
                    );
                }


            }
        }


        // -------------------------
        // Draw route
        // -------------------------

        solutionToDesign.getSolutionRoutes().forEach(route -> {
            g.setColor(getRandomColor());

            for (int i = 0; i < route.getLocationNodeVisitList().size() -1; i++) {
                g.drawLine(route.getLocationNodeVisitList().get(i).getX(),
                        route.getLocationNodeVisitList().get(i).getY(),
                        route.getLocationNodeVisitList().get(i + 1).getX(),
                        route.getLocationNodeVisitList().get(i + 1).getY());
            }

        });





        // -------------------------
        // Draw information box
        // -------------------------

        if (showCustomer && selectedLocationNode != null) {

            int x = selectedLocationNode.getX();
            int y = selectedLocationNode.getY();

            int boxX = x + 10;
            int boxY = y - 20;

            // White background
            g.setColor(Color.WHITE);

            g.fillRect(
                    boxX,
                    boxY,
                    120,
                    55
            );

            // Black border
            g.setColor(Color.BLACK);

            g.drawRect(
                    boxX,
                    boxY,
                    120,
                    55
            );

            // Text
            g.drawString(
                    "X: " + x,
                    boxX + 10,
                    boxY + 15
            );

            g.drawString(
                    "Y: " + y,
                    boxX + 10,
                    boxY + 30
            );

            g.drawString(
                    "Demand: " + selectedLocationNode.getDemand(),
                    boxX + 10,
                    boxY + 45
            );
        }
    }

    /**
     * Generates and returns a new {@code Color} using a shared, fixed-seed
     * {@code Random} instance. Red, green, and blue components are scaled to
     * fall between 0.1 and 0.8, avoiding colors too light to see on a white
     * background, so each route gets a distinct, visible color.
     */
    private Color getRandomColor() {
        // Use shared Random instance to get truly different values for each route
        // Multiply by 0.7 and add 0.1 to ensure colors are dark enough (0.1 to 0.8 range)
        // This prevents light colors that are invisible on white background
        float r = 0.1f + (colorRandom.nextFloat() * 0.7f);
        float g = 0.1f + (colorRandom.nextFloat() * 0.7f);
        float b = 0.1f + (colorRandom.nextFloat() * 0.7f);

        return new Color(r, g, b);
    }
}