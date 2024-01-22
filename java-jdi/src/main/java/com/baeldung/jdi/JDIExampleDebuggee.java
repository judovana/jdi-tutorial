package com.baeldung.jdi;

public class JDIExampleDebuggee {

    private static int SECONDS=20;

    public static void main(String[] args) throws InterruptedException {
        int i=0;
        if (args.length>0) {
            SECONDS=Integer.valueOf(args[0]);
        }
        while(i<SECONDS*10) {
            i++;
            System.out.println(i/10+"/"+SECONDS);
            Thread.sleep(100);
            String jpda = "Java Platform Debugger Architecture";
            System.out.println("Hi Everyone, Welcome to " + jpda); //add a break point here

            String jdi = "Java Debug Interface"; //add a break point here and also stepping in here
            String text = "Today, we'll dive into " + jdi;
            System.out.println(text);
        }
    }
    
}
