package com.pplive.sdk.leacklibrary.heap;

import java.io.File;
import java.io.Serializable;

public class HeapBean implements Serializable {
    File file;
    public HeapBean(File file){
        this.file=file;

    }
}
