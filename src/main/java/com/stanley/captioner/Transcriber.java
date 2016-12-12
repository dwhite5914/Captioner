/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stanley.captioner;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.decoder.adaptation.Stats;
import edu.cmu.sphinx.decoder.adaptation.Transform;
import edu.cmu.sphinx.result.WordResult;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Dan
 */
public class Transcriber
{
    private final File audio;
    private final Configuration config;
    private final File textOut;
    private final File videoIn;
    private final boolean quickTest;

    public Transcriber(File audio, Configuration config, File textOut,
            File videoIn, boolean quickTest)
    {
        this.audio = audio;
        this.config = config;
        this.textOut = textOut;
        this.videoIn = videoIn;
        this.quickTest = quickTest;
    }

    public void start()
    {
        // Create stream speech recognizer.
        StreamSpeechRecognizer recognizer = null;
        try
        {
            recognizer = new StreamSpeechRecognizer(config);
        }
        catch (IOException e)
        {
            System.out.println("Failed to create recognizer.");
        }

        // Open print writer for writing text output.
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(textOut);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Failed to create print writer.");
        }

        // Open stream for first pass.
        InputStream stream = null;
        try
        {
            stream = new FileInputStream(audio);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Failed to stream file.");
        }

        // Initialize loop variables.
        SpeechResult result;
        int resultCount = 0;
        Stats stats = recognizer.createStats(1);

        // Start recognizer for first pass.
        recognizer.startRecognition(stream);
        System.out.println("First pass (stats collection) started.");

        // First pass loop to collect statistics for model adaptation.
        while ((result = recognizer.getResult()) != null)
        {
            try
            {
                stats.collect(result);
            }
            catch (Exception e)
            {
                System.out.println("Failed to collect stats.");
            }

            resultCount++;

            // Toggle for testing.
            if (quickTest && resultCount > 5)
            {
                break;
            }
        }
        // Close recognizer (end of first pass).
        recognizer.stopRecognition();
        System.out.println("Stats collection stopped.");

        // Transform model using model adaptation.
        Transform transform = stats.createTransform();
        recognizer.setTransform(transform);

        // Reopen stream for second pass.
        stream = null;
        try
        {
            stream = new FileInputStream(audio);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Failed to stream file.");
        }

        // Start recognizer for second pass.
        recognizer.startRecognition(stream);
        System.out.println("Second pass started.");

        // Create output text file header.
        writer.printf("%-20s", "WORD:");
        writer.printf("%20s", "CONFIDENCE:");
        writer.printf("%20s", "START TIME:");
        writer.printf("%20s", "END_TIME:");
        writer.println();
        for (int i = 0; i < 80; i++)
        {
            writer.print("-");
        }
        writer.println();

        // Initialize loop variables.
        int wordCount = 0;
        String sentence = "";
        int sentenceLength = 0;
        long sentenceStart = 0;
        long sentenceEnd = 0;
        ArrayList<Sentence> sentences = new ArrayList<>();

        // Second pass loop to calculate sentences.
        RECOG:
        while ((result = recognizer.getResult()) != null)
        {
            for (WordResult wordResult : result.getWords())
            {
                wordCount++;
                String word = wordResult.getWord().toString();
                double confidence = wordResult.getConfidence();
                long startTime = wordResult.getTimeFrame().getStart();
                long endTime = wordResult.getTimeFrame().getEnd();
                writer.printf("%-20s", word);
                writer.printf("%20.1f", confidence);
                writer.printf("%20d", startTime);
                writer.printf("%20d", endTime);
                writer.println();

                if (sentenceLength + word.length() < 40)
                {
                    // Add to current sentence.
                    sentence += " " + word;
                    sentenceLength += word.length();
                    sentenceEnd = endTime;
                }
                else
                {
                    // End of current sentence, store and start a new one.
                    sentences.add(
                            new Sentence(sentence, sentenceStart, sentenceEnd));
                    sentenceStart = sentenceEnd;
                    sentence = "";
                    sentenceLength = 0;
                }

                // Toggle for testing.
                if (quickTest && wordCount > 50)
                {
                    break RECOG;
                }
            }
        }

        // Close print writer and recognizer (end of second pass).
        writer.close();
        recognizer.stopRecognition();
        System.out.println("Second pass stopped.");

        // Create folder for caption images.
        String imageDirPath = FilenameUtils.concat(textOut.getParent(),
                FilenameUtils.getBaseName(textOut.getAbsolutePath()));
        System.out.println(imageDirPath);
        File imageDir = new File(imageDirPath);
        if (!imageDir.exists())
        {
            // Create the folder if it doesn't already exist.
            imageDir.mkdir();
        }

        // Calculate video output path.
        String videoOutPath = FilenameUtils.concat(
                textOut.getParent(),
                FilenameUtils.getBaseName(textOut.getAbsolutePath()) + ".mp4");
        System.out.println(videoOutPath);

        // Initialize a command string for overlaying the captions.
        String commandString = String.format(
                "%s -y -loglevel quiet -i %s",
                new Converter().getFFmpegPath(),
                videoIn.getAbsolutePath());
        System.out.println(commandString);

        // Initialize a complex filter for overlaying the captions.
        String filterString = "-filter_complex";

        // Acquire a probe object for collecting video details.
        Converter converter = new Converter();
        FFprobe ffprobe = null;
        try
        {
            ffprobe = new FFprobe(converter.getFFprobePath());
        }
        catch (IOException e)
        {
            System.out.println("Failed to find ffprobe.");
        }

        // Probe the video for details.
        FFmpegProbeResult probeResult = null;
        try
        {
            probeResult = ffprobe.probe(videoIn.getAbsolutePath());
        }
        catch (IOException e)
        {
            System.out.println("Failed to probe video file.");
        }

        // Get the width and height of the video.
        FFmpegStream videoStream = probeResult.getStreams().get(0);
        int videoWidth = videoStream.width;
        int videoHeight = videoStream.height;

        // Calculate the x and y coordinates of the captions.
        int captionX = (videoWidth / 2) - 220;
        int captionY = videoHeight - 25 - 10;

        // Loop over the sentences, generate captions, and build command string.
        int k = 0;
        for (Sentence s : sentences)
        {
            // Create caption image from sentence.
            BufferedImage bi = new BufferedImage(440, 50, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.setPaint(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, 440, 50);
            g.setPaint(new Color(255, 255, 255, 255));
            g.setFont(new Font("Serif", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            int x = bi.getWidth() - fm.stringWidth(s.text) - 5;
            int y = fm.getHeight() - 5;
            g.drawString(s.text, x, y);
            g.dispose();

            // Write the image to file for future reference.
            String suffix = String.format("caption-%03d.png", k);
            String imagePath = FilenameUtils.concat(imageDirPath, suffix);
            try
            {
                File imageFile = new File(imagePath);
                ImageIO.write(bi, "png", imageFile);
            }
            catch (IOException e)
            {
                System.out.println("Failed to write caption image to file.");
            }

            // Add the caption image path to the command string.
            commandString += " -i " + imagePath;

            // Add an entry to the complex filter with the caption timeframe.
            if (k == 0)
            {
                filterString += String.format(
                        " \"[0:v][1:v] overlay=%d:%d:enable='between(t,%d,%d)'%s",
                        captionX, captionY, s.startTime / 1000, s.endTime / 1000,
                        (k == sentences.size() - 1) ? "\"" : " [tmp];");
            }
            else
            {
                filterString += String.format(
                        " [tmp][%d:v] overlay=%d:%d:enable='between(t,%d,%d)'%s",
                        k + 1, captionX, captionY, s.startTime / 1000, s.endTime / 1000,
                        (k == sentences.size() - 1) ? "\"" : " [tmp];");
            }
            k++;
        }

        // Build final command string.
        String finalCommand = String.format(
                "%s %s -codec:a copy %s",
                commandString, filterString, videoOutPath);

        System.out.println(finalCommand);

        // Attempt to run the final command string to embed the captions.
        try
        {
            Process p = Runtime.getRuntime().exec(finalCommand);
            try
            {
                if (p.waitFor() != 0)
                {
                    // Embedding the captions failed.
                    System.out.println("Image overlay failed.");
                }
            }
            catch (InterruptedException e)
            {
                // Embedding the captions was interrupted.
                System.out.println("Interrupted image overlay.");
            }
        }
        catch (IOException e)
        {
            // Command string failed to execute.
            System.out.println("Failed to execute image overlay.");
        }

        // Delete intermediate audio file.
        audio.delete();

        System.out.println("........................CAPTIONING COMPLETE........................");
    }
}
