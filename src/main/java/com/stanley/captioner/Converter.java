package com.stanley.captioner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

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
        String sep = File.separator;
        return new File(parentPath + sep + fileName);
    }
    
    public void videoToAudio(File videoIn, File audioOut)
    {
        FFmpeg ffmpeg = null;
        try
        {
            System.out.println(getFFmpegPath());
            ffmpeg = new FFmpeg(getFFmpegPath());
        }
        catch (IOException ex)
        {
            System.out.println("Failed to find ffmpeg.");
        }
        
        FFprobe ffprobe = null;
        try
        {
            System.out.println(getFFprobePath());
            ffprobe = new FFprobe(getFFprobePath());
        }
        catch (IOException ex)
        {
            System.out.println("Failed to find ffprobe.");
        }
        
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(videoIn.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(audioOut.getAbsolutePath())
                    .setFormat("wav")
                    .setAudioChannels(1)
                    .setAudioCodec("pcm_s16le")
                    .setAudioSampleRate(16000)
                    .setStrict(FFmpegBuilder.Strict.NORMAL)
                    .done();
        
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
    }
    
    private String getFFmpegPath()
    {
        String os = System.getProperty("os.name");
        File videoIn = getFile("test.mp4");
        String sep = File.separator;
        
        if (os.equals("Windows 10"))
        {
            return toNewFile(videoIn, "ffmpeg_win64" + sep + "bin"
                                + sep + "ffmpeg.exe").getAbsolutePath();
        }
        else
        {
            return toNewFile(videoIn, "ffmpeg_osx" + sep + "ffmepg")
                                .getAbsolutePath();
        }
    }
    
    private String getFFprobePath()
    {
        String os = System.getProperty("os.name");
        File videoIn = getFile("test.mp4");
        String sep = File.separator;
        
        if (os.equals("Windows 10"))
        {
            return toNewFile(videoIn, "ffmpeg_win64" + sep + "bin"
                                + sep + "ffprobe.exe").getAbsolutePath();
        }
        else
        {
            return toNewFile(videoIn, "ffmpeg_osx" + sep + "ffprobe")
                                .getAbsolutePath();
        }
    }
}
