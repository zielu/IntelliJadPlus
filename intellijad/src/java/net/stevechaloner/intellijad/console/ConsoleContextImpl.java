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

package net.stevechaloner.intellijad.console;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import net.stevechaloner.intellijad.IntelliJadResourceBundle;
import net.stevechaloner.intellijad.util.AppInvoker;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

/**
 * @author Steve Chaloner
 */
public class ConsoleContextImpl implements ConsoleContext {
    private final Logger LOG = Logger.getInstance(getClass());

    private final ConsoleTreeNode contextNode;

    private final ConsoleTreeModel consoleTreeModel;

    private final NodeHandler nodeHandler;

    private boolean worthDisplaying;

    /**
     * Initialises a new instance of this class.
     *
     * @param consoleTreeModel the tree model containing the log
     * @param contextNode      the node this log is rooted in
     * @param nodeHandler      the handler for node operations
     */
    ConsoleContextImpl(@NotNull ConsoleTreeModel consoleTreeModel,
                       @NotNull ConsoleTreeNode contextNode,
                       @NotNull NodeHandler nodeHandler) {
        this.consoleTreeModel = consoleTreeModel;
        this.contextNode = contextNode;
        this.nodeHandler = nodeHandler;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWorthDisplaying() {
        return worthDisplaying;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorthDisplaying(boolean worthDisplaying) {
        // if something has already flagged this context of interest,
        // ensure that it remains so
        this.worthDisplaying = this.worthDisplaying || worthDisplaying;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Console is worth displaying", new Throwable("Console worth displaying call site"));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String addMessage(final ConsoleEntryType entryType,
                           String message,
                           Object... parameters) {
        final String formattedMessage = IntelliJadResourceBundle.message(message, parameters);
        if (entryType == ConsoleEntryType.ERROR) {
            LOG.error(formattedMessage);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(formattedMessage);
        }

        AppInvoker.get().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                consoleTreeModel.addMessage(entryType, ConsoleContextImpl.this, formattedMessage);
            }
        });
        return formattedMessage;
    }

    /**
     * {@inheritDoc}
     */
    public String addSectionMessage(final ConsoleEntryType entryType,
                                  final String message,
                                  final Object... parameters) {
        final String formattedMessage = IntelliJadResourceBundle.message(message, parameters);
        if (entryType == ConsoleEntryType.ERROR) {
            LOG.error(formattedMessage);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(formattedMessage);
        }
        AppInvoker.get().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                consoleTreeModel.addSectionMessage(ConsoleContextImpl.this, formattedMessage);
            }
        });
        return formattedMessage;
    }

    public void close() {
        if (!ApplicationManager.getApplication().isUnitTestMode()) {
            AppInvoker.get().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    nodeHandler.select(contextNode);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public ConsoleTreeNode getContextNode() {
        return contextNode;
    }
}
