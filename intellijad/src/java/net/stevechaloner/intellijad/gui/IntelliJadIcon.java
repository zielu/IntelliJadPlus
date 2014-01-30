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

package net.stevechaloner.intellijad.gui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Icons used by the IntelliJad GUI.
 */
public enum IntelliJadIcon {
    ARCHIVE("fileTypes/archive.png"),

    ERROR("general/error.png"),

    INFO("general/information.png"),

    INTELLIJAD_LOGO_13X13("scn-idea-13.png"),

    INTELLIJAD_LOGO_16X16("scn-idea-16-inset.png"),

    INTELLIJAD_LOGO_32X32("scn-idea-32.png"),

    JAVA("fileTypes/java.png"),

    LIBRARIES("modules/library.png"),

    PACKAGE_CLOSED("nodes/TreeOpen.png"),

    PACKAGE_OPEN("nodes/TreeClosed.png")

    ;

    private final String iconPath;
    private final Icon icon;

    private IntelliJadIcon(String iconPath) {
        this.iconPath = iconPath;
        URL resource = IntelliJadIcon.class.getClassLoader().getResource(iconPath);
        if (resource != null) {
            icon = new ImageIcon(resource);
        } else {
            icon = null;
        }
    }

    public String getPath() {
        return iconPath;
    }

    public Icon get() {
        return icon;
    }
}
