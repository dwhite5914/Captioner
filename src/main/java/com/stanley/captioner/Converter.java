package com.stanley.captioner;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import java.io.File;
import java.io.InputStream;

public class Converter
{
    public Converter()
    {
        // Pass.
    }
    
    public File getFile(String fileName)
    {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getPath());
    }
    
    public InputStream getFileAsStream(String fileName)
    {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }
    
    public File toNewFile(File adjacentFile, String fileName)
    {
        File parentFile = adjacentFile.getParentFile();
        String parentPath = parentFile.getPath();
        return new File(parentPath + "\\" + fileName);
    }
    
    public void videoToAudio(File videoIn, File audioOut)
    {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setChannels(1);
        audio.setSamplingRate(16000);
        
        EncodingAttributes encodedAudio = new EncodingAttributes();
        encodedAudio.setFormat("wav");
        encodedAudio.setAudioAttributes(audio);
        
        Encoder encoder = new Encoder();
        try
        {
            encoder.encode(videoIn, audioOut, encodedAudio);
        }
        catch (IllegalArgumentException | EncoderException e)
        {
            System.out.println("Failed to convert.");
        }
    }
}
