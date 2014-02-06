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

package net.stevechaloner.intellijad.config;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import net.stevechaloner.idea.util.properties.DOMable;
import net.stevechaloner.intellijad.IntelliJadConstants;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.rules.RuleContext;
import net.stevechaloner.intellijad.gui.IntelliJadIcon;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic configuation component.
 *
 * @author Steve Chaloner
 */
abstract class ConfigComponent implements Configurable,
                                          JDOMExternalizable
{
    /**
     * The rule execution context.
     */
    private final RuleContext ruleContext = new RuleContext();

    /**
     * The configuration.
     */
    private final Config config = new Config(ruleContext);

    /**
     * The persistable properties.
     */
    private final Map<String, DOMable> domables = new HashMap<String, DOMable>()
    {
        {
            put(config.getPropertyDescriptor().getName(),
                config);
        }
    };

    /**
     * The configuration GUI.
     */
    private ConfigForm form;

    /** {@inheritDoc} */
    public Icon getIcon()
    {
        return IntelliJadIcon.INTELLIJAD_LOGO_32X32.get();
    }

    /** {@inheritDoc} */
    @Nullable
    @NonNls
    public String getHelpTopic()
    {
        return IntelliJadConstants.CONFIGURATION_HELP_TOPIC;
    }

    /** {@inheritDoc} */
    public boolean isModified()
    {
        boolean modified = form != null && form.isModified(config);
        afterIsModified(modified, config);
        return modified;
    }

    protected abstract void afterIsModified(boolean modified, Config config);

    /** {@inheritDoc} */
    public void apply() throws ConfigurationException
    {
        if (form != null)
        {
            form.getData(config);
        }
    }

    /** {@inheritDoc} */
    public void reset()
    {
        if (form != null)
        {
            form.setData(config);
            afterReset(config);
        }
    }

    protected void afterReset(Config config) {}
    
    /** {@inheritDoc} */
    public void disposeUIResources()
    {
        form = null;
    }

    /**
     * Creates a form with no bound project.
     *
     * @return the form
     */
    @NotNull
    protected ConfigForm createForm()
    {
        return createForm(null);
    }

    /**
     * Creates a form with no bound project.
     *
     * @param project the project to bind the form to
     * @return the form
     */
    @NotNull
    protected synchronized ConfigForm createForm(@Nullable Project project)
    {
        if (form != null)
        {
            throw new IllegalArgumentException(IntelliJadResourceBundle.message("error.config-form-already-exists"));
        }
        form = new ConfigForm(project);
        return form;
    }

    // javadoc unnecessary
    @Nullable
    protected ConfigForm getForm()
    {
        return form;
    }

    /** {@inheritDoc} */
    public void readExternal(Element element) throws InvalidDataException
    {
        for (String key : domables.keySet())
        {
            DOMable domable = domables.get(key);
            Element child = element.getChild(key);
            if (child == null)
            {
                child = new Element(key);
                element.addContent(child);
            }
            domable.read(child);
        }
    }

    /** {@inheritDoc} */
    public void writeExternal(Element element) throws WriteExternalException
    {
        for (String key : domables.keySet())
        {
            DOMable domable = domables.get(key);
            element.addContent(domable.write());
        }
    }

    /**
     * Gets the configuration instance.
     *
     * @return the configuration
     */
    Config getConfig()
    {
        return config;
    }
}
