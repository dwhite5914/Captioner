package com.stanley.captioner;

import edu.cmu.sphinx.api.Configuration;
import java.io.File;
import java.io.InputStream;

public class Captioner
{
    public static void main(String args[])
    {
        Converter converter = new Converter();

        File videoIn = converter.getFile("test.mp4");
        File audioOut = converter.toNewFile(videoIn, "test.wav");
        File textOut = converter.toNewFile(videoIn, "output.txt");
        System.out.println(videoIn.getAbsolutePath());
        System.out.println(audioOut.getAbsolutePath());

        converter.videoToAudio(videoIn, audioOut);

        InputStream stream = converter.getFileAsStream("test.wav");
        Configuration config = new Configuration();
        config.setAcousticModelPath(
                "resource:/edu/cmu/sphinx/models/en-us/en-us");
        config.setDictionaryPath(
                "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        config.setLanguageModelPath(
                "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        Transcriber transcriber = new Transcriber("test.wav", config, textOut);
        transcriber.start();
    }
}
