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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 *
 * @author Dan
 */
public class Transcriber
{
    private final String audio;
    private final Configuration config;
    private final File textOut;
    
    public Transcriber(String audio, Configuration config, File textOut)
    {
        this.audio = audio;
        this.config = config;
        this.textOut = textOut;
    }
    
    public void start()
    {
        StreamSpeechRecognizer recognizer = null;
        try
        {
            recognizer = new StreamSpeechRecognizer(config);
        }
        catch (IOException ex)
        {
            System.out.println("Failed to create recognizer.");
        }
        
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(textOut);
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Failed to create print writer.");
        }
        
        Converter converter = new Converter();
        InputStream stream = converter.getFileAsStream(audio);
        
        int wordCount = 0;
        SpeechResult result;
        recognizer.startRecognition(stream);
        System.out.println("First pass started.");
        writer.printf("%-20s", "WORD:");
        writer.printf("%20s", "CONFIDENCE:");
        writer.printf("%20s", "START TIME:");
        writer.printf("%20s", "END_TIME:");
        writer.println();
        for (int i = 0; i < 80; i++) writer.print("-");
        writer.println();
        RECOG: while ((result = recognizer.getResult()) != null)
        {
            for (WordResult wordResult : result.getWords())
            {
                wordCount++;
                writer.printf("%-20s", wordResult.getWord());
                writer.printf("%20.1f", wordResult.getConfidence());
                writer.printf("%20d", wordResult.getTimeFrame().getStart());
                writer.printf("%20d", wordResult.getTimeFrame().getEnd());
                writer.println();
                
                if (wordCount > 5)
                {
                    break RECOG;
                }
            }
        }
        writer.close();
        recognizer.stopRecognition();
        System.out.println("First pass stopped.");
        
//        stream = converter.getFileAsStream(audio);
//        
//        int resultCount = 0;
//        Stats stats = recognizer.createStats(1);
//        recognizer.startRecognition(stream);
//        System.out.println("Stats collection started.");
//        while ((result = recognizer.getResult()) != null)
//        {
//            try
//            {
//                stats.collect(result);
//            }
//            catch (Exception e)
//            {
//                System.out.println("Failed to collect stats.");
//            }
//            
//            resultCount++;
//            if (resultCount > 5)
//            {
//                break;
//            }
//        }
//        recognizer.stopRecognition();
//        System.out.println("Stats collection stopped.");
//        
//        Transform transform = stats.createTransform();
//        recognizer.setTransform(transform);
//        
//        File textOut2 = converter.toNewFile(textOut, "output2.txt");
//        try
//        {
//            writer = new PrintWriter(textOut2);
//        }
//        catch (FileNotFoundException ex)
//        {
//            System.out.println("Failed to create print writer.");
//        }
//        stream = converter.getFileAsStream(audio);
//        
//        wordCount = 0;
//        recognizer.startRecognition(stream);
//        System.out.println("Second pass started.");
//        writer.printf("%-20s", "WORD:");
//        writer.printf("%20s", "CONFIDENCE:");
//        writer.printf("%20s", "START TIME:");
//        writer.printf("%20s", "END_TIME:");
//        writer.println();
//        for (int i = 0; i < 80; i++) writer.print("-");
//        writer.println();
//        RECOG: while ((result = recognizer.getResult()) != null)
//        {
//            for (WordResult wordResult : result.getWords())
//            {
//                wordCount++;
//                writer.printf("%-20s", wordResult.getWord());
//                writer.printf("%20.1f", wordResult.getConfidence());
//                writer.printf("%20d", wordResult.getTimeFrame().getStart());
//                writer.printf("%20d", wordResult.getTimeFrame().getEnd());
//                writer.println();
//                
//                if (wordCount > 5)
//                {
//                    break RECOG;
//                }
//            }
//        }
//        writer.close();
//        recognizer.stopRecognition();
//        System.out.println("Second pass stopped.");
    }
}
