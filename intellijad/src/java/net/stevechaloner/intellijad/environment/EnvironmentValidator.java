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

package net.stevechaloner.intellijad.environment;

import java.io.File;

import javax.swing.JLabel;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MultiLineLabelUI;
import com.intellij.openapi.util.text.StringUtil;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.config.ApplicationConfigComponent;
import net.stevechaloner.intellijad.config.Config;
import net.stevechaloner.intellijad.config.ConfigAccessor;
import net.stevechaloner.intellijad.config.ProjectConfigComponent;
import net.stevechaloner.intellijad.console.ConsoleContext;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import org.jetbrains.annotations.NotNull;

/**
 * Validates the environment and configuration as suitable for decompilation.  If it's not,
 * the user is given the opportunity to rectify the issues.
 *
 * @author Steve Chaloner
 */
public class EnvironmentValidator {
    private static final Logger LOG = Logger.getInstance(EnvironmentValidator.class);
    
    /**
     * Validates the environment prior to decompilation.
     *
     * @param config the configuration
     * @param envContext the environment configuration
     * @param consoleContext the console's logging context for this operation
     * @return true if the decompilation should continue
     */
    public static ValidationResult validateEnvironment(@NotNull Config config,
                                                       @NotNull EnvironmentContext envContext,
                                                       @NotNull ConsoleContext consoleContext)
    {
        String message = null;
        Object[] params = {};
        String jadPath = config.getJadPath();
        if (StringUtil.isEmptyOrSpaces(jadPath)) {
            message = "error.unspecified-jad-path";
        } else {
            File f = new File(jadPath);
            if (!f.exists())
            {
                message = "error.non-existant-jad-path";
                params = new String[]{jadPath};
            }
            else if (!f.isFile())
            {
                message = "error.invalid-jad-path";
                params = new String[]{jadPath};
            }
        }

        ValidationResult result;
        if (message != null) {
            String preparedMessage = IntelliJadResourceBundle.message(message, params);
            if (ApplicationManager.getApplication().isUnitTestMode()) {
                result = new ValidationResult(false, true);
                LOG.error("Validation failed: "+preparedMessage);
            } else {
                result = showErrorDialog(config,
                                         envContext,
                                         consoleContext, preparedMessage);
                consoleContext.addSectionMessage(ConsoleEntryType.ERROR,
                                                 message,
                                                 params);
            }
        } else {
            result = new ValidationResult(true, false);
        }
        return result;
    }

    /**
     * Shows the error dialog, allowing the user to cancel the decompilation or open the config.
     *
     * @param config the configuration
     * @param envContext the environment context
     * @param consoleContext the console's logging context
     * @param message the error message
     * @return the result of the dialog-based operation
     */
    private static ValidationResult showErrorDialog(@NotNull Config config,
                                                    @NotNull EnvironmentContext envContext,
                                                    @NotNull ConsoleContext consoleContext,
                                                    @NotNull String message)
    {
        DialogBuilder builder = new DialogBuilder(envContext.getProject());
        builder.setTitle(IntelliJadResourceBundle.message("plugin.IntelliJad.name"));
        builder.addOkAction().setText(IntelliJadResourceBundle.message("option.open-config"));
        builder.addCancelAction().setText(IntelliJadResourceBundle.message("option.cancel-decompilation"));
        JLabel label = new JLabel(message);
        label.setUI(new MultiLineLabelUI());
        builder.setCenterPanel(label);
        builder.setOkActionEnabled(true);

        ValidationResult result;
        switch (builder.show())
        {
            case DialogWrapper.OK_EXIT_CODE:
                // this will cause recursive correction unless cancel is selected
                Project project = envContext.getProject();
                ConfigAccessor configAccessor = config.isUseProjectSpecificSettings() ? new ProjectConfigComponent(project) : new ApplicationConfigComponent();
                // design point - if this dialog is cancelled, should we assume the decompilation is cancelled?
                ShowSettingsUtil.getInstance().editConfigurable(project,
                                                                configAccessor);
                config.copyFrom(configAccessor.getConfig());
                result = validateEnvironment(config,
                                             envContext,
                                             consoleContext);
                break;
            case DialogWrapper.CANCEL_EXIT_CODE:
            default:
                result = new ValidationResult(false,
                                              true);
                break;
        }

        return result;
    }
}
