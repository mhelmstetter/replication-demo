package com.mongodb.gui.util;

import java.awt.Rectangle;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

public class JDesktopPaneUtils {

    public static void tileInternalFrames(JDesktopPane desktop) {
        Rectangle viewP = desktop.getVisibleRect();
        int totalNonIconFrames = 0;
        JInternalFrame[] frames = desktop.getAllFrames();

        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isIcon()) { // don't include iconified frames...
                totalNonIconFrames++;
            }
        }

        int curCol = 0;
        int curRow = 0;
        int i = 0;

        if (totalNonIconFrames > 0) {
            // compute number of columns and rows then tile the frames
            int numCols = (int) Math.sqrt(totalNonIconFrames);
            int frameWidth = viewP.width / numCols;

            for (curCol = 0; curCol < numCols; curCol++) {

                int numRows = totalNonIconFrames / numCols;
                int remainder = totalNonIconFrames % numCols;

                if ((numCols - curCol) <= remainder) {
                    numRows++; // add an extra row for this guy
                }

                int frameHeight = viewP.height / numRows;

                for (curRow = 0; curRow < numRows; curRow++) {
                    while (frames[i].isIcon()) { // find the next visible frame
                        i++;
                    }

//                    if (frames[i] instanceof DockedInternalFrame) {
//                        ((DockedInternalFrame) frames[i]).internalSetBounds(curCol
//                                * frameWidth, curRow * frameHeight, frameWidth,
//                                frameHeight);
//                    } else {
                        ((JInternalFrame) frames[i]).setBounds(curCol
                                * frameWidth, curRow * frameHeight, frameWidth,
                                frameHeight);
//                    }

                    i++;
                }
            }
        }
    }
}
