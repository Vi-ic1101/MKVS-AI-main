package org.MKVS.function;
import java.util.HashMap;
import org.MKVS.function.MainWork;
public class SettingParameters  {
    private static final HashMap<String,Double> parameters = new HashMap<>();
    static {
        parameters.put("honesty",2.0);
        parameters.put("curiosity",2.0);
        parameters.put("patience",2.0);
        parameters.put("humour",2.0);
    }

    //for parameters update
   public static void updateParameters(String name , double parameterValues){
        if(parameters.containsKey(name)){
            parameters.put(name,parameterValues);
            System.out.println("updated "+name +" to " + parameterValues);
        }else {
            System.out.println("parameters not found" + name);
        }
   }
   //method for current values
    public static HashMap<String,Double> showParameters(){
        parameters.forEach((key,val)-> System.out.println(key + ": "+val));
        return parameters;
    }

}
