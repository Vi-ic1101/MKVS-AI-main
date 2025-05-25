package org.MKVS.function;

// Standard Java I/O
import java.io.IOException;

// CMU Sphinx and javax classes for speech recognition
import javax.sound.sampled.*;
import edu.cmu.sphinx.api.*;

// MaryTTS classes for speech synthesis
import marytts.*;
import marytts.exceptions.*;

// Utility for playing audio
import marytts.util.data.audio.AudioPlayer;


public class MainWork{
    public static void main(String[] st){
        
        //Set audio sample rate to improve audio quality
        System.setProperty("mary.audio.sampleRate","16000");

        //Set audio bit depth
        System.setProperty("mary.audio.bits","16");

        // Disable default audio mixers to avoid audio issues
        System.setProperty("java.sound.useDefaultMixers","false");

        Configuration config = SpeechConfig.getConfiguration();

        try{
            // Create MaryTTS interface for speech synthesis
            MaryInterface marytts= new LocalMaryInterface();

            // Set TTS voice to female  american english 
            marytts.setVoice("cmu-slt-hsmm");

            // Apply audio effect (reduce volume to avoid crackling)
           marytts.setAudioEffects("Volume(amount:0.6)");

            // Speak a message using MaryTTS 
            Speaker.speak(marytts,"Hello. I am Mary and I am the voice of this project");
            CommandProcessor.startListening(marytts,config);
        }catch (Exception e){
            System.err.println("Startup error : "+e.getMessage());
            e.printStackTrace();
        }
    }
}


class SpeechConfig{

        // Static method to create and return a configured CMU Sphinx Configuration object
    public static Configuration getConfiguration(){
        Configuration config = new Configuration();
         // Set the path to the acoustic model used for speech recognition
        config.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
         // Set the path to the pronunciation dictionary (maps words to their phonemes)
        config.setDictionaryPath("src/main/resources/project.dic");

        // Set the path to the language model (defines word probabilities for recognition)
        config.setLanguageModelPath("src/main/resources/project.lm");
        return config;// Set the path to the language model (defines word probabilities for recognition)
    }
}


class Speaker {
    // Static method to synthesize speech from text using MaryTTS
   public static void speak(MaryInterface marytts, String text){
       try{
           // Generate audio from input text using MaryTTS
           AudioInputStream audio = marytts.generateAudio(text);

           // Get the format of the generated audio
           AudioFormat originalFormat = audio.getFormat();
    // Convert to PCM signed format (required for most audio playback systems)
           AudioFormat pcmFormat= new AudioFormat(
                   AudioFormat.Encoding.PCM_SIGNED,         // Encoding type
                   originalFormat.getSampleRate(),16,       // Sample rate (Hz) and Sample size in bits 16
                   originalFormat.getChannels(),            // Number of channels
                   originalFormat.getChannels()*2,            // Frame size (bytes)
                   originalFormat.getSampleRate(),            // Frame rate (same as sample rate)
                   false
           );
            //Convert to PCM format if needed
           AudioInputStream pcmAudio=AudioSystem.getAudioInputStream(pcmFormat, audio);

           DataLine.Info info = new DataLine.Info(SourceDataLine.class,pcmFormat);
           SourceDataLine line =(SourceDataLine) AudioSystem.getLine(info);
           line.open(pcmFormat);
           line.start();

           // Stream audio data to speakers
           byte[] buffer = new byte[4096];
           int bytesRead;
           while((bytesRead=pcmAudio.read(buffer))!=-1){
               line.write(buffer,0,bytesRead);
           }

           // Finish and release resources
           line.drain();
           line.stop();
           line.close();
           pcmAudio.close();
       }catch(Exception e){
           System.err.println("Text to speech error " + e.getMessage());
           e.printStackTrace();
       }
   }
}


class CommandProcessor{
    public static void startListening(MaryInterface marytts,Configuration config) throws IOException{
         // Initialize live speech recognizer with the configuration
        LiveSpeechRecognizer recognizer =new LiveSpeechRecognizer(config);
        // Start listening to live speech (blocking call)
        recognizer.startRecognition(true);
        // Placeholder for recognized speech result
        SpeechResult result;
        // Flag to control recognition loop  
        boolean exit = false;

        while(!exit &&(result = recognizer.getResult())!=null){
             // Get recognized speech as text
            String command = result.getHypothesis().toLowerCase();
            // Handle commands based on voice input
            System.out.println("command regonized : "+ command);

            switch(command){
                // Open Windows Settings
                case "open settings" :
                    Speaker.speak(marytts,"opening settings");
                    Runtime.getRuntime().exec("cmd.exe /c start ms-settings:");
                break;    
                // Close Windows Settings
                case "close settings" :
                    Speaker.speak(marytts,"closing settings");
                    Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM SystemSettings.exe /F");
                break;

                // Open Google Chrome
                case "open chrome" :
                    Speaker.speak(marytts,"opening chrome");
                    Runtime.getRuntime().exec("cmd.exe /c start chrome.exe");
                break;

                // Close Google Chrome
                case "close chrome" :
                    Speaker.speak(marytts,"closing chrome");
                    Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM chrome.exe /F");
                break;
                    
                 // Open whatsapp
                case "open whatsapp" :
                    Speaker.speak(marytts,"opening whatsapp");
                    Runtime.getRuntime().exec("cmd.exe /c start whatsapp:");
                break;
                // closing whatsapp
                case "close whatsapp" :
                    Speaker.speak(marytts,"closing chrome");
                    Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM whatsapp.exe /F");
                break;

                    // to end the program
                case "bye":
                    Speaker.speak(marytts,"thank you");
                    exit=true;
                    break;
            }
        }
    }
}
