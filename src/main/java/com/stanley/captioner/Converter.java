package com.stanley.captioner;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FilenameUtils;

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

    public String getFFmpegPath()
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

    public String getFFprobePath()
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
    
    public static void addCaptions(File video)
    {
        String imageDirPath = FilenameUtils.concat(video.getParent(),
                FilenameUtils.getBaseName(video.getAbsolutePath()));
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists())
        {
            imageDir.mkdir();
        }

        String imageBasePath = FilenameUtils.concat(imageDirPath, "image");
        
        
        
        String commandString = String.format(
                "%s -i %s %s-%%04d.png -loglevel panic",
                new Converter().getFFmpegPath(),
                video.getAbsolutePath(),
                imageBasePath);
    }

    public static void imagesFromVideo(File video)
    {
        String imageDirPath = FilenameUtils.concat(video.getParent(),
                FilenameUtils.getBaseName(video.getAbsolutePath()));
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists())
        {
            imageDir.mkdir();
        }

        String imageBasePath = FilenameUtils.concat(imageDirPath, "image");

        String commandString = String.format(
                "%s -i %s %s-%%04d.png -loglevel panic",
                new Converter().getFFmpegPath(),
                video.getAbsolutePath(),
                imageBasePath);

        System.out.println("COMMAND: " + commandString);
        try
        {
            Process p = Runtime.getRuntime().exec(commandString);
            try
            {
                if (p.waitFor() != 0)
                {
                    System.out.println("Images from video failed.");
                }
            }
            catch (InterruptedException e)
            {
                System.out.println("Interrupted images from video.");
            }
        }
        catch (IOException e)
        {
            System.out.println("Failed to execute images from video.");
        }
    }

    public static void videoFromImages(File video, File videoOut)
    {
        String imageDirPath = FilenameUtils.concat(video.getParent(),
                FilenameUtils.getBaseName(video.getAbsolutePath()));
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists())
        {
            imageDir.mkdir();
        }

        String imageBasePath = FilenameUtils.concat(imageDirPath, "image");

        String commandString = String.format(
                "%s -i %s-%%04d.png %s -loglevel panic",
                new Converter().getFFmpegPath(),
                imageBasePath,
                videoOut.getAbsolutePath());

        System.out.println("COMMAND: " + commandString);
        try
        {
            Process p = Runtime.getRuntime().exec(commandString);
            try
            {
                if (p.waitFor() != 0)
                {
                    System.out.println("Video from images failed.");
                }
            }
            catch (InterruptedException e)
            {
                System.out.println("Interrupted video from images.");
            }
        }
        catch (IOException e)
        {
            System.out.println("Failed to execute video from images.");
        }
    }
}
