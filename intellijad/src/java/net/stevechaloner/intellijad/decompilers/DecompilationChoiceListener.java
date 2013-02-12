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

package net.stevechaloner.intellijad.decompilers;

import net.stevechaloner.intellijad.environment.EnvironmentContext;

/**
 * Listener for navigation-based decompilation that required the
 * user to choose an option.
 *
 * @author Steve Chaloner
 */
public interface DecompilationChoiceListener
{
    /**
     * Decompile the class described in the decompilation descriptor.
     * 
     * @param environmentContext the environment context
     * @param decompilationDescriptor a description of the class to decompile
     */
    void decompile(EnvironmentContext environmentContext,
                   DecompilationDescriptor decompilationDescriptor);
}
