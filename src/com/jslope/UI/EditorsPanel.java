/**
 * Date: 09.08.2005
 */
package com.jslope.UI;

import javax.swing.*;
import java.awt.*;

public class EditorsPanel {
    private static EditorsPanel ourInstance = new EditorsPanel();

    public static EditorsPanel getInstance() {
        return ourInstance;
    }
    private CardLayout cardLayout;

    public JPanel getPanel() {
        return panel;
    }

    private JPanel panel;
    private EditorsPanel() {
        cardLayout = new CardLayout();
        panel = new JPanel(cardLayout);
        panel.add(UserEditor.getUserPanel(), Editors.USER_PANEL.show());
        panel.add(TaskEditor.getTaskPanel(), Editors.TASK_PANEL.show());
        panel.add(TaskDoneEditor.getTaskPanel(), Editors.TASK_DONE_PANEL.show());
        panel.add(SharedTaskEditor.getTaskPanel(), Editors.SHARED_TASK.show());

    }

    public void selectPanel(Editors userPanel) {
        cardLayout.show(panel, userPanel.show());
    }

    public static void select(Editors userPanel) {
        getInstance().selectPanel(userPanel);
    }
}
