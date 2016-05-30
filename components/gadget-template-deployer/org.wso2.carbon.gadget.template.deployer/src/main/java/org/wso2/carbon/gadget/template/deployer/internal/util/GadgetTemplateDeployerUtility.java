package org.wso2.carbon.gadget.template.deployer.internal.util;

import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GadgetTemplateDeployerUtility {
    private GadgetTemplateDeployerUtility() {
    }

    public static void copyFolder(File src, File dest) throws GadgetTemplateDeployerException, IOException {
        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists()) {
                if (!dest.mkdir()) {
                    throw new GadgetTemplateDeployerException("Failed to create destination: " + dest.getAbsolutePath());
                }
            }
            String files[] = src.list();
            if (files != null) {
                for (String file : files) {
                    //construct the src and dest file structure
                    File srcFile = new File(src, file);
                    File destFile = new File(dest, file);
                    //recursive copy
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            try (InputStream in = new FileInputStream(src);
                 OutputStream out = new FileOutputStream(dest);) {
                byte[] buffer = new byte[1024];
                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }
}
