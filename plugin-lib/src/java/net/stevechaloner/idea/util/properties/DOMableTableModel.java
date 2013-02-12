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

package net.stevechaloner.idea.util.properties;

import org.jetbrains.annotations.NotNull;
import org.jdom.Element;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * 
 * @author Steve Chaloner
 */
public class DOMableTableModel extends AbstractDOMable
{
    /**
     *
     */
    public static final String MODEL_CLASS = "model-class";

    /**
     *
     */
    private final DefaultTableModel tableModel;

    /**
     * Initialises a new instance of this class.
     *
     * @param propertyDescriptor the property descriptor
     * @param tableModel the underlying table model
     */
    public DOMableTableModel(@NotNull PropertyDescriptor propertyDescriptor,
                             @NotNull DefaultTableModel tableModel)
    {
        super(propertyDescriptor);
        this.tableModel = tableModel;
    }

    /**
     * Gets the table model this configuration is bound to.
     *
     * @return the table model
     */
    public DefaultTableModel getTableModel()
    {
        return tableModel;
    }

    /**
     * Create a new element detailing a row.
     *
     * @param rowIndex the row of the table model to encapsulate
     * @return a new element
     */
    protected Element createRowElement(int rowIndex)
    {
        Element row = new Element("row");
        for (int i = 0; i < tableModel.getColumnCount(); i++)
        {
            Element cell = new Element("cell");
            Object v = tableModel.getValueAt(rowIndex, i);
            cell.setText(v == null ? "" : v.toString());
            row.addContent(cell);
        }

        return row;
    }

    /** {@inheritDoc} */
    @NotNull
    public Element write()
    {
        Element table = new Element(getPropertyDescriptor().getName());
        table.setAttribute(DOMableType.TYPE,
                           DOMableType.TABLE_MODEL.getName());
        table.setAttribute(MODEL_CLASS,
                           tableModel.getClass().getName());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableModel.getColumnCount(); i++)
        {
            sb.append(DOMableType.getByAnalagousType(tableModel.getColumnClass(i)).getName());
            if (i < tableModel.getColumnCount() - 1)
            {
                sb.append(',');
            }
        }
        table.setAttribute(DOMableCollectionContentType.CONTENT_TYPES,
                           sb.toString());

        for (int i = 0; i < tableModel.getRowCount(); i++)
        {
            table.addContent(createRowElement(i));
        }

        return table;
    }

    /** {@inheritDoc} */
    public void read(@NotNull Element element)
    {
        while (tableModel.getRowCount() > 0)
        {
            tableModel.removeRow(0);
        }

        StringTokenizer st = new StringTokenizer(element.getAttribute(DOMableCollectionContentType.CONTENT_TYPES).getValue(),
                                                 ",");
        List<String> domableTypes = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
            domableTypes.add(st.nextToken());
        }
        for (Element row : (List<Element>)element.getChildren())
        {
            Object[] newRow = new Object[domableTypes.size()];
            for (int i = 0; i < domableTypes.size(); i++)
            {
                List<Element> cells = row.getChildren();
                if (cells.size() != domableTypes.size())
                {
                    throw new IllegalStateException("cell count mismatch, expected " + domableTypes.size() + ", found " + cells.size());
                }
                switch (DOMableType.getByName(domableTypes.get(i)))
                {
                    case STRING:
                        newRow[i] = cells.get(i).getValue();
                        break;
                    case BOOLEAN:
                        newRow[i] = Boolean.valueOf(cells.get(i).getValue());
                        break;
                    case INTEGER:
                        try
                        {
                            newRow[i] = Integer.parseInt(cells.get(i).getValue());
                        }
                        catch (NumberFormatException e)
                        {
                            newRow[i] = 0;
                        }
                        break;
                }
            }
            tableModel.addRow(newRow);
        }
    }

    /** {@inheritDoc} */
    public DefaultTableModel getValue()
    {
        return tableModel;
    }
}
