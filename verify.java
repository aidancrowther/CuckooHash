import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class verify{

    private static cuckooHash<String> hash = new cuckooHash<>();
    private static Random r = new Random();

    public static void main(String[] args){

        int numStrings = 1000;

        String[] strings = new String[numStrings];

        long firstTime = 0;
        long avgTime = 0;
        long worstTime = 0;
        long measuredTime = 0;
        ArrayList<Long> resizeTime = new ArrayList<>();
        long initTime = System.currentTimeMillis();

        long startTime = 0;
        int count = 0;
        int percentage = 0;

        startTime = System.nanoTime();

        while(count < numStrings){

            String toAdd = randomAlphaNumeric(18);
            if(!Arrays.asList(strings).contains(toAdd)) strings[count++] = toAdd;

        }

        System.out.println("Strings generated in: "+(System.nanoTime()-startTime)/1000000+"ms");

        for(int i = 0; i<numStrings; i++){

            if(i == 0){
                startTime = System.nanoTime();
                hash.insert(strings[i]);
                firstTime = System.nanoTime()-startTime;
                avgTime += firstTime;
            }
            else if(hash.getLoad() == (float) 0.4){
                startTime = System.nanoTime();
                hash.insert(strings[i]);
                resizeTime.add(System.nanoTime() - startTime);
                avgTime += resizeTime.get(resizeTime.size()-1);
            }
            else{
                startTime = System.nanoTime();
                hash.insert(strings[i]);
                measuredTime = System.nanoTime()-startTime;
                if(measuredTime > worstTime) worstTime = measuredTime;
                avgTime += measuredTime;
            }
            if(i%(numStrings/100) == 0) System.out.println((percentage++)+"%");

        }

        System.out.print("\n");

        System.out.println("First: "+firstTime/1000+"us");
        System.out.println("Average: "+(avgTime/1000)/numStrings+"us");
        System.out.println("Worst: "+worstTime/1000+"us");
        System.out.print("Resize times: ");
        
        for(int i=0; i<resizeTime.size(); i++){
            System.out.print(resizeTime.get(i)/1000+"us ");
        }

        System.out.println("\nTotal: "+(System.currentTimeMillis()-initTime)/1000+"s");

    }


    public static String randomAlphaNumeric(int count){

        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {

            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));

        }

        return builder.toString();

    }

}