import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class LCG {
    
    int numGenerated;
    
    public static void main(String[] args) {

        Scanner reader = new Scanner(System.in);  // Reading from System.in
        
        System.out.println("Choose a random function:");
        System.out.println("1 - Standard Rand Function");
        System.out.println("2 - LGC");
        System.out.println("3 - LGC(RANDU)");
        int functionNum = reader.nextInt();        
        
        System.out.println("\nHow many numbers should be generated? (10,000 Reccomended)");
        int numsGenerated = reader.nextInt();
        
        System.out.println("\nThe seed used will be: 123456789");
        long seed = 123456789;  
        
        if(functionNum==1){
            rand(numsGenerated,seed);
        }else if(functionNum==2 || functionNum==3){
            lcgTest(numsGenerated,seed,functionNum);
        }else{
            System.out.println("Please Enter a number 1-3");
            System.exit(0);
        }
        
        System.out.println("\nChoose a test to run:");
        System.out.println("1 - Kolmogorov-Smirnov Test");
        System.out.println("2 - Chi-Square Test");
        System.out.println("3 - Runs Test");
        System.out.println("4 - Autocorrelations Test");
        int testNum = reader.nextInt();
        
        if(testNum==1){//Kolmogorov-Smirnov
            ks();
        }else if(testNum == 2){//Chi-Square
            chiSquare();
        }else if(testNum == 3){//Runs
            runs(numsGenerated);
        }else if(testNum == 4){//Autocorrelations
             System.out.println("Enter an l value:");
             int l = reader.nextInt();
            ac(numsGenerated,l);
        }else{
            System.out.println("Please enter number 1-4");
            System.exit(0);
        }
    }
    
     public static void rand(int n,long seed){
        try (PrintWriter writer = new PrintWriter("randoms.txt")) {
            Random rnd = new Random(seed);
            for(int i=0; i<n; i++){
                writer.println(rnd.nextDouble());
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void lcgTest(int n,long seed,int function){
        BigInteger a;
        BigInteger c;
        BigInteger m;
        BigInteger x;
        BigDecimal result;
        BigDecimal max;//Number to divide by to get all answers < 1
        if(function==2){//LCG
            x = new BigInteger(Long.toString(seed));
            a = new BigInteger("101427");
            c = new BigInteger ("321");
            m = new BigInteger("65536");
            max = new BigDecimal("65536");

        }else{//RANDU
            x = new BigInteger(Long.toString(seed));
            a = new BigInteger("65539");
            c = new BigInteger("0");
            m = new BigInteger("2147483648");
            max = new BigDecimal("2147483648");//Number to divide by to get all answers < 1
        }
        //run lcg test
        try (PrintWriter writer = new PrintWriter("randoms.txt")) {
            for(int i=0; i<n; i++){
                result = new BigDecimal(x.toString());
                result = result.divide(max);
                writer.println(result);
                x = ((a.multiply(x)).add(c)).mod(m);
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }        
{
            
        }
    }
    
    private static void ks() {
        File file = new File("randoms.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean eighty = false, ninty = false, nintyFive = false;
            int i = 0;
            double dPlus = 0;
            double dMinus = 0;
            double temp;
            double D;
            double N = 100;
            ArrayList<Double> data = new ArrayList<>();
            while ((line = br.readLine()) != null && i<N) {//add to arraylist to sort
                data.add(Double.parseDouble(line));
                i++;
            }
            Collections.sort(data);
            for(i=0; i<N; i++){
                temp = (double)(i+1)/N-data.get(i);
                if(temp>dPlus){
                    dPlus = temp;
                }
                temp = (double)data.get(i)-i/N;
                if(temp>dMinus){
                    dMinus = temp;
                }
            }
            D = Math.max(dPlus, dMinus);
            System.out.println("D value: "+D);
            if(D<(1.07/Math.sqrt(N))){
                eighty = true;
            }
            if(D<(1.22/Math.sqrt(N))){
                ninty =  true;
            }
            if(D<(1.36/Math.sqrt(N))){
                nintyFive = true;
            }
            results(eighty,ninty,nintyFive);
        } catch (IOException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void chiSquare() {
        int[] y = new int[10];//actual
        double c = 0;
        int e;//expected
        File file = new File("randoms.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int rounded;
            int total = 0;
            boolean eighty = false, ninty = false, nintyFive = false;
            while ((line = br.readLine()) != null) {
                rounded = Integer.parseInt(line.substring(2,3));//round down to the hundreths place
                y[rounded]++;//count the number in each division
                total++;
            }
            e = total/10;
            for(int i=0;i<10;i++){
                c += Math.pow((y[i] - e),2)/e;//calculate C value                
            }
            System.out.println("c value: " + c);
            //Using 9 as degrees of freedom (10 cells)
            if(c<14.648){
                eighty = true;
            }
            if(c<16.919){
                ninty = true;
            }
            if(c<19.023){
                nintyFive = true;
            }
            results(eighty,ninty,nintyFive);
        } catch (IOException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void runs(int N) {
        double expVar;
        double expMu;
        double Z;
        double cur = 0;
        double last;
        int runs = 1;
        boolean positive = true;
        File file = new File("randoms.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean eighty = false, ninty = false, nintyFive = false;
            while ((line = br.readLine()) != null) {
                last = cur;
                cur = Double.parseDouble(line);
                if((cur>last&&!positive)||(cur<last&&positive)){//end of run
                    runs++;
                    positive = !positive;//change run
                }
            }
            expMu =(double)(2*N-1)/3;
            expVar =(double)(16*N-29)/90;
            Z = (runs-expMu)/expVar;
            System.out.println("Z value: " + Z);
            if(Z>-1.29&&Z<1.29){
                eighty = true;
            }
            if(Z>-1.65&&Z<1.65){
                ninty = true;
            }
            if(Z>=-1.96&&Z<1.96){
                nintyFive = true;
            }            
            results(eighty,ninty,nintyFive);
        } catch (IOException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void ac(int N,int l) {
        int i = 0;//starting point
        double M;
        int index = 0;
        double prev;
        double next = 0;
        double sum = 0;
        double rho;
        double sigma;
        double Z;
        M = (int)Math.floor((N-i)/l-1);
        System.out.println("Using " + i + " as i");
        File file = new File("randoms.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean eighty = false, ninty = false, nintyFive = false;
            while ((line = br.readLine()) != null) {
                if(index%l==0){
                    prev = next;
                    next = Double.parseDouble(line);
                    sum += prev*next;
                }
                index++;
            }
            rho = 1/(M+1)*sum-.25;
            sigma = Math.sqrt(13*M+7)/(12*(M+1));
            Z = rho/sigma;
            System.out.println("Z value: " + Z);
            if(Z>-1.29&&Z<1.29){
                eighty = true;
            }
            if(Z>-1.65&&Z<1.65){
                ninty = true;
            }
            if(Z>=-1.96&&Z<1.96){
                nintyFive = true;
            } 
            results(eighty,ninty,nintyFive);
        } catch (IOException ex) {
            Logger.getLogger(LCG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void results(boolean eighty, boolean ninty, boolean nintyFive){
        if(nintyFive){
            System.out.println("Significance at 95%");
        }
        if(ninty){
            System.out.println("Significance at 90%");
        }
        if(eighty){
            System.out.println("Significance at 80%");
        }
        if(!eighty&&!ninty&&!nintyFive){
            System.out.println("No Signifcance at 80%, 90%, or 95%");
        }
    }
}