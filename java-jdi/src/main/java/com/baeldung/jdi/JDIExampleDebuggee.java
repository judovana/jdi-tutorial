package com.baeldung.jdi;

public class JDIExampleDebuggee {

    private final static int SECONDS=10;

    public static void main(String[] args) throws InterruptedException {
        int i=0;
        while(i<SECONDS*10) {
            i++;
            Thread.sleep(100);
            String jpda = "Java Platform Debugger Architecture";
            System.out.println("Hi Everyone, Welcome to " + jpda); //add a break point here

            String jdi = "Java Debug Interface"; //add a break point here and also stepping in here
            String text = "Today, we'll dive into " + jdi;
            System.out.println(text);
        }
    }
    
}
