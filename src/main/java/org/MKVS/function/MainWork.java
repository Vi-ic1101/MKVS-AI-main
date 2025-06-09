package org.MKVS.function;

// Standard Java I/O
import java.io.IOException;
import java.lang.annotation.Target;
import java.util.Arrays;

import javax.sound.sampled.*;


// MaryTTS classes for speech synthesis
import marytts.*;
import marytts.exceptions.*;

// Utility for playing audio
import marytts.util.data.audio.AudioPlayer;

import org.json.JSONObject;
import org.vosk.*;




public class MainWork{
    public static void main(String[] st){

        //Set audio sample rate to improve audio quality
        System.setProperty("mary.audio.sampleRate","16000");

        //Set audio bit depth
        System.setProperty("mary.audio.bits","16");

        // Disable default audio mixers to avoid audio issues
        System.setProperty("java.sound.useDefaultMixers","false");

        //Configuration config = SpeechConfig.getConfiguration();

        try{
            // Create MaryTTS interface for speech synthesis
            MaryInterface marytts= new LocalMaryInterface();

            // Set TTS voice to female  american english
            marytts.setVoice("cmu-slt-hsmm");

            // Apply audio effect (reduce volume to avoid crackling)
           marytts.setAudioEffects("Volume(amount:0.6)");

            // Speak a message using MaryTTS
            Speaker.speak(marytts,"Hello. I am Mary and I am the voice of this project");
            Speaker.speak(marytts,"And I am listening.");
            CommandProcessor.startListening(marytts);
        }catch (Exception e){
            System.err.println("Startup error : "+e.getMessage());
            e.printStackTrace();
        }
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

           // Stream audio data to
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


    public static void startListening(MaryInterface marytts) throws IOException, LineUnavailableException {
        int count = 0;
        //using vosk model
        LibVosk.setLogLevel(LogLevel.WARNINGS);
        Model model=new Model("src/main/resources/vosk-model-en-in-0.5");
        Recognizer recognizer = new Recognizer(model,16000.0f);

        //using default microphone
        TargetDataLine microphone;
        AudioFormat  format=new AudioFormat(16000.0f,16,1,true,false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,format);
        microphone =(TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        System.out.println("listening...");

        byte[] buffer = new byte[4096];
        // Flag to control recognition loop
        boolean exit = false;
// &&(result = recognizer.getResult())!=null
        while(!exit){
            int bytesRead = microphone.read(buffer,0, buffer.length);
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String resultJson = recognizer.getResult();
//                System.out.println("Raw Vosk result: " + resultJson);

                // Parse JSON properly
                String command = "";
                try {
                    JSONObject obj = new JSONObject(resultJson);
                    command = obj.optString("text", "").trim().toLowerCase();
                } catch (Exception e) {
                    System.err.println("JSON parsing error: " + e.getMessage());
                }

                System.out.println("Recognized command: '" + command + "'");

                if (command.isEmpty()) {
                    if(count < 5){
                       marytts.setAudioEffects("Volume(amount:0.6)");

                        Speaker.speak(marytts,"sir are you there ");
                        count++;
                        System.out.println("No command recognized, continuing..."+ count);
                    }else{
                        Speaker.speak(marytts,"looks like no one is here");
                        break;
                    }
                    continue;
                }

                 switch (command) {
                     // Open Windows Settings
                     case "open settings":
                         Speaker.speak(marytts, "opening settings");
                         Runtime.getRuntime().exec("cmd.exe /c start ms-settings:");
                         break;
                     // Close Windows Settings
                     case "close settings":
                         Speaker.speak(marytts, "closing settings");
                         Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM SystemSettings.exe /F");
                         break;

                     // Open Google Chrome
                     case "open chrome":
                         Speaker.speak(marytts, "opening chrome");
                         Runtime.getRuntime().exec("cmd.exe /c start chrome.exe");
                         break;

                     // Close Google Chrome
                     case "close chrome":
                         Speaker.speak(marytts, "closing chrome");
                         Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM chrome.exe /F");
                         break;

                     // Open whatsapp
                     case "open whatsapp":
                         Speaker.speak(marytts, "opening whatsapp");
                         Runtime.getRuntime().exec("cmd.exe /c start whatsapp:");
                         break;
                     // closing whatsapp
                     case "close whatsapp":
                         Speaker.speak(marytts, "closing whatsapp");
                         Runtime.getRuntime().exec("cmd.exe /c TASKKILL /IM whatsapp.exe /F");
                         break;

                     // to end the program
                     case "bye":
                         Speaker.speak(marytts, "thank you");
                         exit = true;
                         break;
                 }
             }
        }
        recognizer.close();
        microphone.stop();
        microphone.close();
    }
}
