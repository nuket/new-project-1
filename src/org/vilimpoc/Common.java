/**
    Copyright (c) 2017 Max Vilimpoc

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package org.vilimpoc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

class Common {
    // Unfortunately, we have to track this ourselves.
//    static Tab           currentTab;
//    static TabController currentTabController;

    private static String lastUsedFolder;

    private static final String UNTITLED_FILE_PREFIX = "FabrikUml-";
    private static final String UNTITLED_FILE_SUFFIX = ".txt";

    static final String WORK_FOLDER = ".FabrikUml";

    static final KeyCombination NEW_  = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    static final KeyCombination SAVE  = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    static final KeyCombination CLOSE = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    
    // Figure out what $HOME folder is.
    //
    // Ref: http://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
    // Requires Java 8.
    static Path getWorkFolder() {
        return Paths.get(System.getProperty("user.home"), Common.WORK_FOLDER);
    }
    
    static File getUntitledFile() throws IOException {
        return File.createTempFile(UNTITLED_FILE_PREFIX, UNTITLED_FILE_SUFFIX, getWorkFolder().toFile());
    }
    
    static {
        
    }
}