import java.util.Scanner;
import java.io.*;
import java.util.*;
class OwlClassHandler
{
    //ArrayList storing claas names on right side and left side(right side has leaves)
    private ArrayList<String> rightSide=new ArrayList<String>();
    private ArrayList<String> leftSide=new ArrayList<String>();
    //primitives=left side-right side
    private ArrayList<String> primitives=new ArrayList<String>();
    private HashMap<String,String> primitivesCode= new HashMap<String,String>();
    //Array storing trees
    private HashMap<String,Node> allClasses=new HashMap<String,Node>();
    //axiom based
    private ArrayList<String> saxClasses=new ArrayList<String>();
    private ArrayList<String> caxClasses=new ArrayList<String>();
    private ArrayList<String> naxClasses=new ArrayList<String>();
    //datatype property
    private HashMap<String,Node> dtpprop=new HashMap<String,Node>();
    //the main function
    public void main(String fname)throws IOException
    {
        Scanner sc=new Scanner(new FileInputStream(fname));
        String temp="";
        ArrayList<String> s=new ArrayList<String>();
        String set="";
        int a=0;
        int b=0;
        int c=0;
        String stdtps="";
        boolean classStart=false;
        boolean dtps=false;
        while(sc.hasNextLine())
        {
            temp=sc.nextLine();
            if((!dtps) && (!classStart))
            {
                if(temp.indexOf("<owl:Class rdf:ID=")>=0)
                {
                    if(temp.indexOf("/>")>=0)
                    {
                        String classKey=temp.substring(temp.indexOf("\"",temp.indexOf("rdf:ID"))+1,temp.indexOf("\"",temp.indexOf("\"",temp.indexOf("rdf:ID"))+2));
                        if(!rightSide.contains(classKey))
                        {
                            rightSide.add(classKey);
                        }
                    }
                    else
                    {
                        set=set.concat("\n"+temp);
                        classStart=true;
                    }
                }
                else if(temp.indexOf("<owl:DatatypeProperty rdf:ID=")>=0)
                {
                    if(!classStart)
                    {
                        dtps=true;
                        stdtps=temp;
                    }
                }
            }
            else if(classStart)
            {
                if(temp.indexOf("</owl:Class>")>=0)
                {
                    set=set.concat("\n"+temp);
                    s.add(set);
                    classStart=false;
                    set=new String();
                }
                else
                {
                    set=set.concat("\n"+temp);
                }
            }
            else if(dtps)
            {
                if(temp.indexOf("</owl:DatatypeProperty")>=0)
                {
                    dtps=false;
                    datatypePropertyHandler(stdtps);
                    stdtps="";
                }
                else
                {
                    stdtps=stdtps.concat("\n"+temp);
                }
            }
        }
        for(int k=0;k<s.size();k++)
        {
            stringToTree(s.get(k));
        }
        getPrimitives();
        print();
        System.out.println("Primitives:");
        for(int i=0;i<primitives.size();i++)
        {
            System.out.println(primitives.get(i));
        }
        /*System.out.println("LeftSide:");
        leftSide=new ArrayList<String>(getLeaves());
        for(int j=0;j<leftSide.size();j++)
        {
            System.out.println(leftSide.get(j));
        }*/
        setPrimitivesRelation();
        getMaxCodeLengthAndAllign();
        System.out.println("Primitives with Codes:");
        for(int j=0;j<primitives.size();j++)
        {
            System.out.println(primitives.get(j)+": "+primitivesCode.get(primitives.get(j)));
        }
    }
    
    //alligns the primitive bit code to be of equal length
    private void getMaxCodeLengthAndAllign()
    {
        int max=0;
        for(int j=0;j<primitives.size();j++)
        {
            if(max<((primitivesCode.get(primitives.get(j))).length()))
            {
                max=(primitivesCode.get(primitives.get(j))).length();
            }
        }
        for(int j=0;j<primitives.size();j++)
        {
            if(((primitivesCode.get(primitives.get(j))).length())<max)
            {
                String add="";
                int dif=max-((primitivesCode.get(primitives.get(j))).length());
                for(int count=0;count<dif;count++)
                {
                    add=add+"0";
                }
                add=add+(primitivesCode.get(primitives.get(j)));
                primitivesCode.put(primitives.get(j),add);
            }
        }
    }
    
    //datatype property handler
    private void datatypePropertyHandler(String st)
    {
        int i=st.indexOf('=');
        String id=st.substring(i+2,st.indexOf('\"',i+2));
        i=st.indexOf('=',i);
        String domain=st.substring(i+3,st.indexOf('\"',i+3));
        i=st.indexOf(';',st.indexOf('=',i));
        String range=st.substring(i+1,st.indexOf('\"',i));
        Node d=new Node("Domain");
        d.rightChild=new Node(domain);
        (d.rightChild).parent=d;
        Node r=new Node("Range");
        r.rightChild=new Node(range);
        (r.rightChild).parent=r;
        Node h=new Node("   has  ");
        h.leftChild=new Node(d);
        (h.leftChild).parent=h;
        h.rightChild=new Node(r);
        (h.rightChild).parent=h;
        dtpprop.put(id,h);
    }

    //negation operation handler
    public String operationNegationOf(String input)
    {
        String result="";
        for(int i=0;i<input.length();i++)
        {
            char u=input.charAt(i);
            if(u=='0')
            {
                result=result+"0";
            }
            if(u=='1')
            {
                result=result+"1'";
            }
            if(u=='X')
            {
                if('l'==input.charAt(i+1))
                {
                    i++;
                    result=result+"1'l";
                }
                else if('u'==input.charAt(i))
                {
                    i++;
                    result=result+"1'u";
                }
                else
                {
                    result=result+"1'";
                }
            }
        }
        return result;
    }
    
    private String integerToString(int n)
    {
        String result="";
        while(n>0)
        {
            if((n%2)==0)
            {
                result="0"+result;
            }
            else
            {
                result="1"+result;
            }
            n=n/2;
        }
        return result;
    }
    
    private int bitStringToInteger(String s)
    {
        int result=0;
        int c=1;
        for(int i=s.length()-1;i>=0;i--)
        {
            if(s.charAt(i)=='1')
            {
                result=result+c;
            }
            c=c*2;
        }
        return result;
    }
    
    //operation intersection of handler
    public String operationIntersectionOf(String upper,String lower)
    {
        String result="";
        int j=0;
        for(int i=0;i<upper.length();i++)
        {
            char u=upper.charAt(i);
            if(u=='1')
            {
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                        j++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                        }
                    }
                }
                else
                {
                    result=result+"1";
                    if(!(Character.isDigit(upper.charAt(i+1))))
                    {
                        if(!((lower.charAt(j)=='1') && (Character.isDigit(lower.charAt(j+1)))))
                        {
                            result=result+"'";
                        }
                        if(lower.charAt(j)=='0')
                        {
                            result=result+"x";
                        }
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                        j++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                        }
                    }
                }
            }
            if(u=='0')
            {
                if(lower.charAt(j)=='1')
                {
                    result=result+"X";
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                    }
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                    }
                    j++;
                }
                if(lower.charAt(j)=='0')
                {
                    result=result+"0";
                    j++;
                }
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                        }
                    }
                }
            }
            if(u=='X')
            {
                if(lower.charAt(j)=='1')
                {
                    result=result+"X";
                    j++;
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                            j++;
                        }
                        if((!(Character.isDigit(lower.charAt(j)))) && (lower.charAt(j)!='X'))
                        {
                            j++;
                        }
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                }
                if(lower.charAt(j)=='0')
                {
                    result=result+"X";
                    j++;
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                        if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                        {
                            i++;
                        }
                    }
                }
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    j=j+1;
                    if(Character.isDigit(upper.charAt(i+1)))
                    {
                        if(!(Character.isDigit(lower.charAt(j))))
                        {
                            if(lower.charAt(j)=='l')
                            {
                                result=result+"l";
                                j++;
                            }
                            else if(lower.charAt(j)=='u')
                            {
                                result=result+"u";
                                j++;
                                if((!(Character.isDigit(lower.charAt(j)))) && (lower.charAt(j)!='X'))
                                {
                                    j++;
                                    result=result+"l";
                                }
                            }
                        }
                    }
                    else
                    {
                        i++;
                        if(upper.charAt(i)=='l')
                        {
                            if(Character.isDigit(lower.charAt(j)))
                            {
                                result=result+"l";
                                j++;
                            }
                            else
                            {
                                j++;
                                if(lower.charAt(j)=='u')
                                {
                                    result=result+"ul";
                                    j++;
                                    if(Character.isDigit(lower.charAt(j+1)))
                                    {
                                        //
                                    }
                                    else if(lower.charAt(j+1)!='X')
                                    {
                                        j++;
                                    }
                                }
                                else
                                {
                                    result=result+"l";
                                    j++;
                                }
                            }
                        }
                        else
                        {
                            if(Character.isDigit(upper.charAt(i+1)))
                            {
                                result=result+"u";
                                if(!(Character.isDigit(lower.charAt(j))))
                                {
                                    if(lower.charAt(j)=='l')
                                    {
                                        result=result+"l";
                                        j++;
                                    }
                                    else if(lower.charAt(j+1)!='X')
                                    {
                                        if(!Character.isDigit(lower.charAt(j+1)))
                                        {
                                            j++;
                                            result=result+"l";
                                            j++;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                i++;
                                result=result+"ul";
                                j++;
                                if(!(Character.isDigit(lower.charAt(j))))
                                {
                                    j++;
                                    if(!(Character.isDigit(lower.charAt(j))))
                                    {
                                        j++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    //operation union of handler
    public String operationUnionOf(String upper,String lower)
    {
        String result="";
        int j=0;
        for(int i=0;i<upper.length();i++)
        {
            char u=upper.charAt(i);
            if(u=='1')
            {
                if(lower.charAt(j)=='1')
                {
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            result=result+"1'";
                            j++;
                            if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                            {
                                j++;
                            }
                            j++;
                        }
                        else
                        {
                            result=result+"X";
                        }
                    }
                    else if(Character.isDigit(lower.charAt(j+1)))
                    {
                        result=result+"1";
                    }
                    else
                    {
                        result=result+"X";
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    j++;
                }
                if(lower.charAt(j)=='0')
                {
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    result=result+"X";
                    j++;
                }
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                            j++;
                        }
                    }
                }
            }
            if(u=='0')
            {
                if(lower.charAt(j)=='1')
                {
                    result=result+"X";
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                    }
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                    }
                    j++;
                }
                if(lower.charAt(j)=='0')
                {
                    result=result+"0";
                    j++;
                }
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                    {
                        j++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                        }
                    }
                }
            }
            if(u=='X')
            {
                if(lower.charAt(j)=='1')
                {
                    result=result+"X";
                    j++;
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                        if((!(Character.isDigit(lower.charAt(j+1)))) && (lower.charAt(j+1)!='X'))
                        {
                            j++;
                            j++;
                        }
                        if((!(Character.isDigit(lower.charAt(j)))) && (lower.charAt(j)!='X'))
                        {
                            j++;
                        }
                    }
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                    }
                }
                if(lower.charAt(j)=='0')
                {
                    result=result+"X";
                    j++;
                    if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                    {
                        i++;
                        if((!(Character.isDigit(upper.charAt(i+1)))) && (upper.charAt(i+1)!='X'))
                        {
                            i++;
                        }
                    }
                }
                if(lower.charAt(j)=='X')
                {
                    result=result+"X";
                    j=j+1;
                    if(Character.isDigit(upper.charAt(i+1)))
                    {
                        if(!(Character.isDigit(lower.charAt(j))))
                        {
                            if(lower.charAt(j)=='l')
                            {
                                result=result+"l";
                                j++;
                            }
                            else if(lower.charAt(j)=='u')
                            {
                                result=result+"u";
                                j++;
                                if((!(Character.isDigit(lower.charAt(j)))) && (lower.charAt(j)!='X'))
                                {
                                    j++;
                                    result=result+"l";
                                }
                            }
                        }
                    }
                    else
                    {
                        i++;
                        if(upper.charAt(i)=='l')
                        {
                            if(Character.isDigit(lower.charAt(j)))
                            {
                                result=result+"l";
                                j++;
                            }
                            else
                            {
                                j++;
                                if(lower.charAt(j)=='u')
                                {
                                    result=result+"ul";
                                    j++;
                                    if(Character.isDigit(lower.charAt(j+1)))
                                    {
                                        //
                                    }
                                    else if(lower.charAt(j+1)!='X')
                                    {
                                        j++;
                                    }
                                }
                                else
                                {
                                    result=result+"l";
                                    j++;
                                }
                            }
                        }
                        else
                        {
                            if(Character.isDigit(upper.charAt(i+1)))
                            {
                                result=result+"u";
                                if(!(Character.isDigit(lower.charAt(j))))
                                {
                                    if(lower.charAt(j)=='l')
                                    {
                                        result=result+"l";
                                        j++;
                                    }
                                    else if(lower.charAt(j+1)!='X')
                                    {
                                        if(!Character.isDigit(lower.charAt(j+1)))
                                        {
                                            j++;
                                            result=result+"l";
                                            j++;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                i++;
                                result=result+"ul";
                                j++;
                                if(!(Character.isDigit(lower.charAt(j))))
                                {
                                    j++;
                                    if(!(Character.isDigit(lower.charAt(j))))
                                    {
                                        j++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    //to print all class definitions
    private void print()
    {
        int i=1;
        System.out.println("ALL");
        for (Iterator<Map.Entry<String, Node>> it = allClasses.entrySet().iterator();it.hasNext();)
        {
            System.out.println(i+" TREE :");
            Map.Entry<String, Node> entry = it.next();
            Node n = entry.getValue();
            System.out.print(entry.getKey()+" is ");
            if(!(n==null))
            {
                printNode(n);
            }
            System.out.println();
            i++;
        }
    }

    private void printNode(Node x)
    {
        Stack<Node> ptemp=new Stack<Node>();
        ptemp.push(x);
        while(!ptemp.empty())
        {
            Node n=ptemp.pop();
            if(n.visit==true)
            {
                System.out.print(" "+n.name);
            }
            else
            {
                n.visit=true;
                if(n.rightChild!=null)
                {
                    ptemp.push(n.rightChild);
                }
                ptemp.push(n);
                if(n.leftChild!=null)
                {
                    ptemp.push(n.leftChild);
                }
            }
        }
    }

    //subclass block to stack
    private Stack<String> subClassStringToStack(String str)
    {
        Stack<String> st=new Stack<String>();
        Scanner sc=new Scanner(str);
        String temp="";
        String s="";
        String xe="";
        boolean owl=false;
        while(sc.hasNextLine())
        {
            temp=sc.nextLine();
            if(owl==false)
            {
                if(temp.indexOf("<owl:")>=0)
                {
                    s=temp;
                    int kh=temp.indexOf(" ",temp.indexOf("<owl:")+5);
                    int kj=temp.indexOf(">",temp.indexOf("<owl:"));
                    if((kj>=0) && (kh>=0))
                    {
                        kh=(kj<kh)?kj:kh;
                    }
                    else if(kj>=0)
                    {
                        kh=kj;
                    }
                    xe=temp.substring(temp.indexOf("<owl:")+5,kh);
                    owl=true;
                }
                else if(temp.indexOf("<rdfs:subClassOf rdf:resource=")>=0)
                {
                    st.push(temp);
                }
            }
            else
            {
                if(temp.indexOf("</owl:"+xe)>=0) 
                {
                    s=s+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    xe="";
                    xe=new String("");
                    owl=false;
                }
                else
                {
                    s=s+temp;
                }
            }
        }
        ArrayList<String> ts=new ArrayList<String>();
        while(!(st.empty()))
        {
            temp=st.pop();
            str=str.replace(temp,"");
            ts.add(temp);
        }
        st.push(str);
        for(int i=0;i<ts.size();i++)
        {
            st.push(ts.get(i));
        }
        return st;
    }

    //intersection of block to stack
    private Stack<String> intersectionOfStack(String str)
    {
        Stack<String> st=new Stack<String>();
        Scanner sc=new Scanner(str);
        String temp="";
        String s="";
        String xe="";
        boolean rdfs=false;
        boolean owl=false;
        while(sc.hasNextLine())
        {
            temp=sc.nextLine();
            if(temp.indexOf("intersectionOf")>=0)
            {
                continue;
            }
            if((rdfs==false) && (owl==false))
            {
                if(temp.indexOf("owl:Class rdf:about")>=0)
                {
                    st.push(temp);
                    continue;
                }
                if(temp.indexOf("<rdfs:subClassOf>")>=0)
                {
                    s=temp;
                    rdfs=true;
                }
                if(temp.indexOf("<owl:")>=0)
                {
                    s=temp;
                    int kh=temp.indexOf(" ",temp.indexOf("<owl:")+5);
                    int kj=temp.indexOf(">",temp.indexOf("<owl:"));
                    if((kj>=0) && (kh>=0))
                    {
                        kh=(kj<kh)?kj:kh;
                    }
                    else if(kj>=0)
                    {
                        kh=kj;
                    }
                    xe=temp.substring(temp.indexOf("<owl:")+5,kh);
                    owl=true;
                }
            }
            else if(rdfs==true)
            {
                if(temp.indexOf("</rdfs:subClassOf>")>=0)
                {
                    s=s+"\n"+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    rdfs=false;
                }
                else
                {
                    s=s+"\n"+temp;
                }
            }
            else if(owl==true)
            {
                if(temp.indexOf("</owl:"+xe)>=0) 
                {
                    s=s+"\n"+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    xe="";
                    xe=new String("");
                    owl=false;
                }
                else
                {
                    s=s+"\n"+temp;
                }
            }
        }
        ArrayList<String> ts=new ArrayList<String>();
        while(!(st.empty()))
        {
            temp=st.pop();
            str=str.replace(temp,"");
            ts.add(temp);
        }
        for(int i=0;i<ts.size();i++)
        {
            st.push(ts.get(i));
        }
        return st;
    }

    //intersection of block to tree
    private Node intersectionOfHandler(String str)
    {
        Stack<String> st=intersectionOfStack(str);
        Node presentNode=new Node(" intersection of ");
        Node parent=null;
        String temp="";
        int co=0;
        while(!st.empty())
        {
            temp=st.pop();
            if(temp.indexOf("owl:Class rdf:about")>=0)
            {
                if(co==0)
                {
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("rdf:about="))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("rdf:about="))));
                    presentNode.leftChild=new Node(c);
                    (presentNode.leftChild).parent=presentNode;
                }
                else
                {
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("rdf:about="))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("rdf:about="))));
                    presentNode.rightChild=new Node(c);
                    (presentNode.rightChild).parent=presentNode;
                }
            }
            else if(temp.indexOf("Restriction")>=0)
            {
                if(temp.indexOf("someValuesFrom")>0)
                {
                    parent=new Node(" some values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                else if(temp.indexOf("allValuesFrom")>0)
                {
                    parent=new Node(" all values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                else if(temp.indexOf("hasValue")>0)
                {
                    parent=new Node(" has value ");
                    String c=temp.substring(temp.indexOf(">",temp.indexOf("hasValue"))+2,temp.indexOf("</owl:hasValue")-2);
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                String c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                parent.leftChild=new Node(c);
                (parent.leftChild).parent=parent;
                if(co==0)
                {
                    presentNode.leftChild=parent;
                    (presentNode.leftChild).parent=presentNode;
                }
                else
                {
                    presentNode.rightChild=parent;
                    (presentNode.rightChild).parent=presentNode;
                }
            }
            co++;
        }
        return presentNode;
    }

    //bain block string to stack
    private Stack<String> stringToStack(String str)
    {
        Stack<String> st=new Stack<String>();
        Scanner sc=new Scanner(str);
        String temp="";
        String s="";
        String xe="";
        boolean rdfs=false;
        boolean owl=false;
        while(sc.hasNextLine())
        {
            temp=sc.nextLine();
            if((rdfs==false) && (owl==false))
            {
                if(temp.indexOf("owl:Class rdf:ID")>=0)
                {
                    st.push(temp);
                    continue;
                }
                if(temp.indexOf("<rdfs:subClassOf>")>=0)
                {
                    s=temp;
                    rdfs=true;
                    continue;
                }
                if(temp.indexOf("<owl:")>=0)
                {
                    s=temp;
                    int kh=temp.indexOf(" ",temp.indexOf("<owl:")+5);
                    int kj=temp.indexOf(">",temp.indexOf("<owl:"));
                    if((kj>=0) && (kh>=0))
                    {
                        kh=(kj<kh)?kj:kh;
                    }
                    else if(kj>=0)
                    {
                        kh=kj;
                    }
                    xe=temp.substring(temp.indexOf("<owl:")+5,kh);
                    owl=true;
                }
                else if(temp.indexOf("<rdfs:subClassOf rdf:resource=")>=0)
                {
                    st.push(temp);
                }
            }
            else if(rdfs==true)
            {
                if(temp.indexOf("</rdfs:subClassOf>")>=0)
                {
                    s=s+"\n"+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    rdfs=false;
                }
                else
                {
                    s=s+"\n"+temp;
                }
            }
            else if(owl==true)
            {
                if(temp.indexOf("</owl:"+xe)>=0) 
                    {
                        s=s+"\n"+temp;
                        st.push(s);
                        s="";
                        s=new String("");
                        xe="";
                        xe=new String("");
                        owl=false;
                    }
                    else
                    {
                        s=s+"\n"+temp;
                    }
            }
        }
        ArrayList<String> ts=new ArrayList<String>();
        while(!(st.empty()))
        {
            temp=st.pop();
            str=str.replace(temp,"");
            ts.add(temp);
        }
        st.push(str);
        for(int i=0;i<ts.size();i++)
        {
            st.push(ts.get(i));
        }
        return st;
    }
    
    //union of block to stack
    private Stack<String> unionOfStack(String str)
    {
        Stack<String> st=new Stack<String>();
        Scanner sc=new Scanner(str);
        String temp="";
        String s="";
        String xe="";
        boolean rdfs=false;
        boolean owl=false;
        while(sc.hasNextLine())
        {
            temp=sc.nextLine();
            if(temp.indexOf("unionOf")>=0)
            {
                continue;
            }
            if((rdfs==false) && (owl==false))
            {
                if(temp.indexOf("owl:Class rdf:about")>=0)
                {
                    st.push(temp);
                    continue;
                }
                if(temp.indexOf("<rdfs:subClassOf>")>=0)
                {
                    s=temp;
                    rdfs=true;
                }
                if(temp.indexOf("<owl:")>=0)
                {
                    s=temp;
                    int kh=temp.indexOf(" ",temp.indexOf("<owl:")+5);
                    int kj=temp.indexOf(">",temp.indexOf("<owl:"));
                    if((kj>=0) && (kh>=0))
                    {
                        kh=(kj<kh)?kj:kh;
                    }
                    else if(kj>=0)
                    {
                        kh=kj;
                    }
                    xe=temp.substring(temp.indexOf("<owl:")+5,kh);
                    owl=true;
                }
            }
            else if(rdfs==true)
            {
                if(temp.indexOf("</rdfs:subClassOf>")>=0)
                {
                    s=s+"\n"+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    rdfs=false;
                }
                else
                {
                    s=s+"\n"+temp;
                }
            }
            else if(owl==true)
            {
                if(temp.indexOf("</owl:"+xe)>=0) 
                {
                    s=s+"\n"+temp;
                    st.push(s);
                    s="";
                    s=new String("");
                    xe="";
                    xe=new String("");
                    owl=false;
                }
                else
                {
                    s=s+"\n"+temp;
                }
            }
        }
        ArrayList<String> ts=new ArrayList<String>();
        while(!(st.empty()))
        {
            temp=st.pop();
            str=str.replace(temp,"");
            ts.add(temp);
        }
        for(int i=0;i<ts.size();i++)
        {
            st.push(ts.get(i));
        }
        return st;
    }

    //union of block to tree
    private Node unionOfHandler(String str)
    {
        Stack<String> st=unionOfStack(str);
        Node presentNode=new Node(" union of ");
        Node parent=null;
        String temp="";
        int co=0;
        while(!st.empty())
        {
            temp=st.pop();
            if(temp.indexOf("owl:Class rdf:about")>=0)
            {
                if(co==0)
                {
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("rdf:about="))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("rdf:about="))));
                    presentNode.leftChild=new Node(c);
                    (presentNode.leftChild).parent=presentNode;
                }
                else
                {
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("rdf:about="))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("rdf:about="))));
                    presentNode.rightChild=new Node(c);
                    (presentNode.rightChild).parent=presentNode;
                }
            }
            else if(temp.indexOf("Restriction")>=0)
            {
                if(temp.indexOf("someValuesFrom")>0)
                {
                    parent=new Node(" some values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                else if(temp.indexOf("allValuesFrom")>0)
                {
                    parent=new Node(" all values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                else if(temp.indexOf("hasValue")>0)
                {
                    parent=new Node(" has value ");
                    String c=temp.substring(temp.indexOf(">",temp.indexOf("hasValue"))+2,temp.indexOf("</owl:hasValue")-2);
                    parent.rightChild=new Node(c);
                    (parent.rightChild).parent=parent;
                }
                String c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                parent.leftChild=new Node(c);
                (parent.leftChild).parent=parent;
                if(co==0)
                {
                    presentNode.leftChild=parent;
                    (presentNode.leftChild).parent=presentNode;
                }
                else
                {
                    presentNode.rightChild=parent;
                    (presentNode.rightChild).parent=presentNode;
                }
            }
            co++;
        }
        return presentNode;
    }

    //subclass block to tree
    private Node subClassStringToTree(String st)
    {
        Stack<String> classStack=subClassStringToStack(st);
        Node parent;
        Node presentNode=null;
        String temp="";
        while(!classStack.empty())
        {
            temp=classStack.pop();
            if(temp.indexOf("subClassOf rdf:resource=")>=0)
            {
                if(presentNode==null)
                {
                    presentNode=new Node(" sub class of ");
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("subClassOf rdf:resource="))+1,temp.indexOf("/",temp.indexOf("rdf:resource="))-1);
                    presentNode.rightChild=new Node(c);
                    (presentNode.rightChild).parent=presentNode;
                }
                else
                {
                    parent=new Node(" sub class of ");
                    parent.leftChild=presentNode;
                    presentNode.parent=parent;
                    presentNode=new Node(parent);
                    String c=temp.substring(temp.indexOf("#",temp.indexOf("subClassOf rdf:resource="))+1,temp.indexOf("/",temp.indexOf("rdf:resource="))-1);
                    presentNode.rightChild=new Node(c);
                    (presentNode.rightChild).parent=presentNode;
                }
            }
            else if(temp.indexOf("unionOf")>0)
            {
                cax=true;
                parent=unionOfHandler(temp);
                if(presentNode==null)
                {
                    presentNode=new Node(parent);
                }
                else
                {
                    Node tempNode=new Node("   &   ");
                    parent.parent=tempNode;
                    tempNode.leftChild=presentNode;
                    tempNode.rightChild=parent;
                    presentNode.parent=tempNode;
                    presentNode=new Node(tempNode);
                }
            }
            else if(temp.indexOf("intersectionOf")>0)
            {
                cax=true;
                parent=intersectionOfHandler(temp);
                if(presentNode==null)
                {
                    presentNode=new Node(parent);
                }
                else
                {
                    Node tempNode=new Node("   &   ");
                    parent.parent=tempNode;
                    tempNode.leftChild=presentNode;
                    tempNode.rightChild=parent;
                    presentNode.parent=tempNode;
                    presentNode=new Node(tempNode);
                }
            }
            else if(temp.indexOf("Restriction")>0)
            {
                if(temp.indexOf("someValuesFrom")>0)
                {
                    parent=new Node(" some values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    presentNode=new Node(parent);
                }
                else if(temp.indexOf("allValuesFrom")>0)
                {
                    parent=new Node(" all values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    presentNode=new Node(parent);
                }
                else if(temp.indexOf("hasValue")>0)
                {
                    parent=new Node(" has value ");
                    String c=temp.substring(temp.indexOf(">",temp.indexOf("hasValue"))+2,temp.indexOf("</owl:hasValue")-2);
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    presentNode=new Node(parent);
                }
            }
        }
        return presentNode;
    }
    
    private boolean sax;
    private boolean cax;
    
    //main block to tree
    private   void stringToTree(String st)
    {
        Stack<String> classStack=stringToStack(st);
        Node parent;
        Node presentNode=null;
        String classKey="";
        String temp="";
        cax=false;
        sax=false;
        while(!classStack.empty())
        {
            temp=classStack.pop();
            if(temp.indexOf("subClassOf")>0)
            {
                if(temp.indexOf("<rdfs:subClassOf>")>=0)
                {
                    if(presentNode==null)
                    {
                        presentNode=new Node(" sub class of ");
                        parent=subClassStringToTree(temp);
                        presentNode.rightChild=parent;
                    }
                    else
                    {
                        parent=subClassStringToTree(temp);
                        parent.parent=new Node(" sub class of ");
                        (parent.parent).rightChild=parent;
                        (parent.parent).leftChild=presentNode;
                        presentNode.parent=parent.parent;
                        presentNode=parent.parent;
                    }
                }
                if(temp.indexOf("subClassOf rdf:resource=")>=0)
                {
                    sax=true;
                    if(presentNode==null)
                    {
                        presentNode=new Node(" sub class of ");
                        String c=temp.substring(temp.indexOf("#",temp.indexOf("subClassOf rdf:resource="))+1,temp.indexOf("/",temp.indexOf("rdf:resource="))-1);
                        presentNode.rightChild=new Node(c);
                    }
                    else
                    {
                        parent=new Node(" sub class of ");
                        parent.leftChild=presentNode;
                        presentNode.parent=parent;
                        presentNode=new Node(parent);
                        String c=temp.substring(temp.indexOf("#",temp.indexOf("subClassOf rdf:resource="))+1,temp.indexOf("/",temp.indexOf("rdf:resource="))-1);
                        presentNode.rightChild=new Node(c);
                    }
                }
            }
            else if(temp.indexOf("unionOf")>0)
            {
                parent=unionOfHandler(temp);
                if(presentNode==null)
                {
                    presentNode=new Node(parent);
                }
                else
                {
                    Node tempNode=new Node("   &   ");
                    parent.parent=tempNode;
                    tempNode.leftChild=presentNode;
                    tempNode.rightChild=parent;
                    presentNode.parent=tempNode;
                    presentNode=new Node(tempNode);
                }
            }
            else if(temp.indexOf("intersectionOf")>0)
            {
                parent=intersectionOfHandler(temp);
                if(presentNode==null)
                {
                    presentNode=new Node(parent);
                }
                else
                {
                    Node tempNode=new Node("   &   ");
                    parent.parent=tempNode;
                    tempNode.leftChild=presentNode;
                    tempNode.rightChild=parent;
                    presentNode.parent=tempNode;
                    presentNode=new Node(tempNode);
                }
            }
            else if(temp.indexOf("Restriction")>0)
            {
                if(temp.indexOf("someValuesFrom")>0)
                {
                    parent=new Node(" some values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    if(presentNode==null)
                    {
                        presentNode=new Node(parent);
                    }
                    else
                    {
                        Node tempNode=new Node("   &   ");
                        parent.parent=tempNode;
                        tempNode.leftChild=presentNode;
                        tempNode.rightChild=parent;
                        presentNode.parent=tempNode;
                        presentNode=new Node(tempNode);
                    }
                }
                else if(temp.indexOf("allValuesFrom")>0)
                {
                    parent=new Node(" all values from ");
                    String c=temp.substring(temp.indexOf("#")+1,temp.indexOf("\"",temp.indexOf("#")));
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    if(presentNode==null)
                    {
                        presentNode=new Node(parent);
                    }
                    else
                    {
                        Node tempNode=new Node("   &   ");
                        parent.parent=tempNode;
                        tempNode.leftChild=presentNode;
                        tempNode.rightChild=parent;
                        presentNode.parent=tempNode;
                        presentNode=new Node(tempNode);
                    }
                }
                else if(temp.indexOf("hasValue")>0)
                {
                    parent=new Node(" has value ");
                    String c=temp.substring(temp.indexOf(">",temp.indexOf("hasValue"))+2,temp.indexOf("</owl:hasValue")-2);
                    parent.rightChild=new Node(c);
                    c=temp.substring(temp.indexOf("#",temp.indexOf("onProperty"))+1,temp.indexOf("\"",temp.indexOf("#",temp.indexOf("onProperty"))));
                    parent.leftChild=new Node(c);
                    if(presentNode==null)
                    {
                        presentNode=new Node(parent);
                    }
                    else
                    {
                        Node tempNode=new Node("   &   ");
                        parent.parent=tempNode;
                        tempNode.leftChild=presentNode;
                        tempNode.rightChild=parent;
                        presentNode.parent=tempNode;
                        presentNode=new Node(tempNode);
                    }
                }
            }
            if(classKey=="")
            {
                if(temp.indexOf("rdf:ID=")>0)
                {
                    classKey=temp.substring(temp.indexOf("\"",temp.indexOf("rdf:ID"))+1,temp.indexOf("\"",temp.indexOf("\"",temp.indexOf("rdf:ID"))+2));
                    if(!rightSide.contains(classKey))
                    {
                        rightSide.add(classKey);
                    }
                }
            }
        }
        if(presentNode==null)
        {
            return;
        }
        if(allClasses.get(classKey)==null)
        {
            allClasses.put(classKey,presentNode);
            if(cax==true)
            {
                caxClasses.add(classKey);
            }
            else if(sax==true)
            {
                saxClasses.add(classKey);
            }
            else
            {
                naxClasses.add(classKey);
            }
        }
        else
        {
            Node t=allClasses.get(classKey);
            Node a=new Node("   &   ");
            a.leftChild=new Node(t);
            (a.leftChild).parent=a;
            a.rightChild=new Node(presentNode);
            (a.rightChild).parent=a;
            allClasses.put(classKey,a);
            if(cax==true)
            {
                caxClasses.add(classKey);
            }
            else if(sax==true)
            {
                saxClasses.add(classKey);
            }
            else
            {
                naxClasses.add(classKey);
            }
        }
        cax=false;
        sax=false;
    }

    //get to return the tree if input string class name is complex or simple axiom in terms of subclass 
    private Node getCAXOrSAXTree(String s)
    {
        if(caxClasses.indexOf(s)>=0)
        {
            return allClasses.get(s);
        }
        else if(saxClasses.indexOf(s)>=0)
        {
            return allClasses.get(s);
        }
        return null;
    }

    //primitives arranged level wise and integer code assigned to all primitives
    private void setPrimitivesRelation()
    {
        ArrayList<String> lp=new ArrayList<String>();
        HashMap<String,Integer> pl=new HashMap<String,Integer>();
        for(int i=0;i<primitives.size();i++)
        {
            Node tn=getCAXOrSAXTree(primitives.get(i));
            if(tn==null)
            {
                pl.put(primitives.get(i),1);
            }
            else
            {
                String[] tp=getParentClasses(tn);
                if(tp.length<1)
                {
                    pl.put(primitives.get(i),1);
                }
                else
                {
                    lp.add(primitives.get(i));
                }
            }
        }
        int cl;
        boolean flag=true;
        up:
        while(!(lp.isEmpty()))
        {
            cl=0;
            flag=true;
            for(int i=0;i<lp.size();i++)
            {
                String[] tp=getParentClasses(getCAXOrSAXTree(lp.get(i)));
                for(int k=0;k<tp.length;k++)
                {
                    if((pl.get(tp[k]))==null)
                    {
                        flag=false;
                    }
                    else
                    {
                        if((cl-1)<(pl.get(tp[k])))
                        {
                            cl=(pl.get(tp[k]))+1;
                        }
                    }
                }
                if(flag)
                {
                    pl.put(lp.get(i),cl);
                    lp.remove(i);
                    continue up;
                }
            }
        }
        int le=2;
        int code=1;
        for(int i=0;i<primitives.size();i++)
        {
            if((pl.get(primitives.get(i)))==1)
            {
                primitivesCode.put(primitives.get(i),integerToString(code));
                code=2*code;
            }
        }
        while((primitivesCode.size())!=(primitives.size()))
        {
            for(int i=0;i<primitives.size();i++)
            {
                if((pl.get(primitives.get(i)))==le)
                {
                    String[] tp=getParentClasses(getCAXOrSAXTree(primitives.get(i)));
                    int sum=code;
                    for(int pk=0;pk<tp.length;pk++)
                    {
                        sum=sum+bitStringToInteger(primitivesCode.get(tp[pk]));
                    }
                    primitivesCode.put(primitives.get(i),integerToString(sum));
                    code=2*code;
                }
            }
            le++;
        }
    }

    //to get parent classes of a class
    public String[] getParentClasses(String str)
    {
        Node n=getCAXOrSAXTree(str);
        if(n==null)
        {
            return null;
        }
        return getParentClasses(n);
    }
    private String[] getParentClasses(Node n)
    {
        ArrayList<String> p=new ArrayList<String>();
        Stack<Node> st=new Stack<Node>();
        st.push(n);
        Stack<Node> st1=new Stack<Node>();
        out:
        while(!st.empty())
        {
            Node temp=st.pop();
            if(temp.name==" sub class of ")
            {
                st1.push(temp);
                continue out;
            }
            if(!(temp.leftChild==null))
            {
                st.push(temp.leftChild);
            }
            if(!(temp.rightChild==null))
            {
                st.push(temp.rightChild);
            }
        }
        while(!st1.empty())
        {
            Node temp=st1.pop();
            if((primitives.indexOf(temp.name))>=0)
            {
                p.add(temp.name);
            }
            if(!(temp.leftChild==null))
            {
                st1.push(temp.leftChild);
            }
            if(!(temp.rightChild==null))
            {
                st1.push(temp.rightChild);
            }
        }
        String[] fs=new String[p.size()];
        for(int j=0;j<p.size();j++)
        {
            fs[j]=p.get(j);
        }
        return fs;
    }

    //to read to tree and return class names at leaves
    private String[] readNode(Node n)
    {
        ArrayList<String> ls=new ArrayList<String>();
        Stack<Node> st=new Stack<Node>();
        st.push(n);
        while(!st.empty())
        {
            Node temp=st.pop();
            if(!(temp.leftChild==null))
            {
                if(((temp.leftChild).leftChild==null) && ((temp.leftChild).rightChild==null))
                {
                    if(ls.indexOf((temp.leftChild).name)<0)
                    {
                        ls.add((temp.leftChild).name);
                    }
                }
                else
                {
                    st.push(temp.leftChild);
                }
            }
            if(!(temp.rightChild==null))
            {
                if(((temp.rightChild).leftChild==null) && ((temp.rightChild).rightChild==null))
                {
                    if(temp.name==" has value ")
                    {
                        //do nothing
                    }
                    else
                    {
                        if(ls.indexOf((temp.rightChild).name)<0)
                        {
                            ls.add((temp.rightChild).name);
                        }
                    }
                }
                else
                {
                    st.push(temp.rightChild);
                }
            }
        }
        String[] fs=new String[ls.size()];
        for(int j=0;j<ls.size();j++)
        {
            fs[j]=ls.get(j);
        }
        return fs;
    }

    //to get all the leaf classes
    private ArrayList<String> getLeaves()
    {
        ArrayList<String> t=new ArrayList<String>();
        for (Iterator<Map.Entry<String, Node>> it = allClasses.entrySet().iterator();it.hasNext();)
        {
            Map.Entry<String, Node> entry = it.next();
            Node n = entry.getValue();
            String cn=entry.getKey();
            if(!(n==null))
            {
                String[] temp=readNode(n);
                for(int j=0;j<temp.length;j++)
                {
                    if(t.indexOf(temp[j])<0)
                    {
                        if(!(temp[j].equals(cn)))
                        {
                            t.add(temp[j]);
                        }
                    }
                }
            }
        }
        return t;
    }

    //to set primitives
    private void getPrimitives()
    {
        ArrayList<String> leaves=getLeaves();
        for(int j=0;j<rightSide.size();j++)
        {
            if(leaves.indexOf(rightSide.get(j))>=0)
            {
                leaves.remove(leaves.indexOf(rightSide.get(j)));
            }
            else
            {
                leaves.add(rightSide.get(j));
            }
        }
        String[] ap=new String[leaves.size()];
        for(int j=0;j<leaves.size();j++)
        {
            ap[j]=leaves.get(j);
        }
        primitives.clear();
        if(primitives.isEmpty())
        {
            for(int i=0;i<ap.length;i++)
            {
                primitives.add(ap[i]);
            }
        }
    }
}
