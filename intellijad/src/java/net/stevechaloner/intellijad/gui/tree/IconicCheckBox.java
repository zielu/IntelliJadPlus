/*
 * Copyright 2007 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package net.stevechaloner.intellijad.gui.tree;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A component that renders, in order, a checkbox, an icon and some text.
 */
public class IconicCheckBox
{
    /**
     * The checkbox.
     */
    private JCheckBox checkBox;

    /**
     * The label.
     */
    private JLabel label;

    /**
     * The container for the checkbox and label.
     */
    private JPanel contentPane;

    /**
     * Gets the checkbox.
     *
     * @return the checkbox
     */
    public JCheckBox getCheckBox()
    {
        return checkBox;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public JLabel getLabel()
    {
        return label;
    }

    /**
     * Gets the content pane.
     *
     * @return the content pane
     */
    public JPanel getContentPane()
    {
        return contentPane;
    }
}
