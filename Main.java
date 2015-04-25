import java.io.*;
class Main
{
    public static void main(String[] args)throws IOException
    {
        OwlClassHandler o1=new OwlClassHandler();
        if(args.length==0){
        	System.out.println("Usage: Main <inputfile>");
        	return;
        }
        String fileName="Unsaved Document 1.txt";
        o1.main(fileName);
    }
}
