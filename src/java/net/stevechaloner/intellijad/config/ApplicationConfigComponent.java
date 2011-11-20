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

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;

import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * The component for the application-level configuration.
 *
 * @author Steve Chaloner
 */
public class ApplicationConfigComponent implements ApplicationComponent,
                                                   ConfigAccessor,
                                                   JDOMExternalizable
{
    /**
     * The name of the component
     */
    @NonNls
    private static final String COMPONENT_NAME = "IntelliJadConfigComponent";

    /**
     * The generic configuration component.
     */
    private final ConfigComponent configComponent = new ConfigComponent()
    {
        /** {@inheritDoc} */
        @Nls
        public String getDisplayName()
        {
            return "IntelliJad";
        }

        /** {@inheritDoc} */
        @NotNull
        public JComponent createComponent()
        {
            ConfigForm form = getForm();
            return form == null ? createForm().getRoot() : form.getRoot();
        }
    };

    /** {@inheritDoc} */
    @NonNls
    @NotNull
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

    /** {@inheritDoc} */
    public void initComponent()
    {
    }

    /** {@inheritDoc} */
    public void disposeComponent()
    {
    }

    /** {@inheritDoc} */
    public Icon getIcon()
    {
        return configComponent.getIcon();
    }

    @Nls
    /** {@inheritDoc} */
    public String getDisplayName()
    {
        return configComponent.getDisplayName();
    }

    /** {@inheritDoc} */
    @Nullable
    @NonNls
    public String getHelpTopic()
    {
        return configComponent.getHelpTopic();
    }

    /** {@inheritDoc} */
    public JComponent createComponent()
    {
        return configComponent.createComponent();
    }

    /** {@inheritDoc} */
    public boolean isModified()
    {
        return configComponent.isModified();
    }

    /** {@inheritDoc} */
    public void apply() throws ConfigurationException
    {
        configComponent.apply();
    }

    /** {@inheritDoc} */
    public void reset()
    {
        configComponent.reset();
    }

    /** {@inheritDoc} */
    public void disposeUIResources()
    {
        configComponent.disposeUIResources();
    }

    /** {@inheritDoc} */
    public void readExternal(Element element) throws InvalidDataException
    {
        configComponent.readExternal(element);
    }

    /** {@inheritDoc} */
    public void writeExternal(Element element) throws WriteExternalException
    {
        configComponent.writeExternal(element);
    }

    // javadoc unnecessary
    public Config getConfig()
    {
        return configComponent.getConfig();
    }
}
