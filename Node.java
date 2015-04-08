class Node
{
    public String name;
    public Node parent;
    public Node rightChild;
    public Node leftChild;
    public boolean visit;
    public Node(String s)
    {
        this.name=s;
        this.parent=null;
        this.leftChild=null;
        this.rightChild=null;
        this.visit=false;
    }

    public Node(Node t)
    {
        this.name=t.name;
        this.parent=t.parent;
        this.leftChild=t.leftChild;
        this.rightChild=t.rightChild;
        this.visit=t.visit;
    }
}