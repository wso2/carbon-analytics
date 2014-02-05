/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.dashboard.social.ui;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ImageScalerUtil {
    private int thubmnailArraySize = 0;

    public InputStream scaleImage(InputStream input, int height, int width, int quality)
            throws Exception {

        Image image = ImageIO.read(new BufferedInputStream(input));
        if (image == null) {
            throw new Exception("Unable to change/upload profile Image");
        }
        // Maintain Aspect ratio
        int thumbHeight = height;
        int thumbWidth = width;
        double thumbRatio = (double) width / (double) height;

        double imageRatio = (double) image.getWidth(null) / (double) image.getHeight(null);
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        BufferedImage thumb =
                new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumb.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        JPEGTranscoder transcoder = new JPEGTranscoder();
        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(
                Math.max(0, Math.min(quality, 100)) / 100.0f));
        transcoder.writeImage(thumb, new TranscoderOutput(output));
        thubmnailArraySize = output.size();
        return new ByteArrayInputStream(output.toByteArray());
    }

    public int getThubmnailArraySize() {
        return thubmnailArraySize;
    }
}

