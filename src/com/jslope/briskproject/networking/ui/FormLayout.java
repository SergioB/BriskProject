package com.jslope.briskproject.networking.ui;

import java.awt.*;

/**
 * A layout manager that lays out components along a central axis
 */
class FormLayout implements LayoutManager {
    public Dimension preferredLayoutSize(Container parent) {
        Component[] components = parent.getComponents();
        left = 0;
        right = 0;
        height = 0;
        for (int i = 0; i < components.length; i += 2) {
            Component cleft = components[i];
            Component cright = components[i + 1];

            Dimension dleft = cleft.getPreferredSize();
            Dimension dright = cright.getPreferredSize();
            left = Math.max(left, dleft.width);
            right = Math.max(right, dright.width);
            height = height + Math.max(dleft.height,
                    dright.height);
        }
        return new Dimension(left + GAP + right, height);
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        preferredLayoutSize(parent); // sets left, right

        Component[] components = parent.getComponents();

        Insets insets = parent.getInsets();
        int xcenter = insets.left + left;
        int y = insets.top;

        for (int i = 0; i < components.length; i += 2) {
            Component leftComponent = components[i];
            Component rightComponent = components[i + 1];

            Dimension leftDimensions = leftComponent.getPreferredSize();
            Dimension rightDimensions = rightComponent.getPreferredSize();

            int height = Math.max(leftDimensions.height,
                    rightDimensions.height);

            leftComponent.setBounds(xcenter - leftDimensions.width, y + (height -
                    leftDimensions.height) / 2, leftDimensions.width, leftDimensions.height);

            rightComponent.setBounds(xcenter + GAP, y + (height
                    - rightDimensions.height) / 2, rightDimensions.width, rightDimensions.height);
            y += height;
        }
    }

    public void addLayoutComponent(String name,
                                   Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    private int left;
    private int right;
    private int height;
    private static final int GAP = 6;
}