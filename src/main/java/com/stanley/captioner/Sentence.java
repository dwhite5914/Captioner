/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stanley.captioner;

/**
 *
 * @author User
 */
public class Sentence
{
    public String text;
    public long startTime;
    public long endTime;
    
    public Sentence(String text, long startTime, long endTime)
    {
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
