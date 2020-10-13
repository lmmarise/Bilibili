package org.tsb.bilibili.merge;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ExampleUnitTest {
    @Test
    public void getBingBgImg() throws IOException {
        File file = new File("D:\\\\0");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.createNewFile());
    }
}