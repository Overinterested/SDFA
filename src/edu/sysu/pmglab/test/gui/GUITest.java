package edu.sysu.pmglab.test.gui;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.SDFViewer;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-27 08:25
 * @description
 */
public class GUITest {
    public static void main(String[] args) throws IOException {
        SDFViewer.view(new File("/Users/wenjiepeng/Desktop/SDFA/test/extract/test1"));
    }
}