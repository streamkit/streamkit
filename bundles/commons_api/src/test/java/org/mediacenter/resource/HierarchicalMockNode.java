package org.mediacenter.resource;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.apache.sling.commons.testing.jcr.MockNode;

/**
 * Created by IntelliJ IDEA.
 * User: ddascal
 * Date: 11/28/12
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class HierarchicalMockNode extends MockNode
{
    private Map<String, Node> children;
    private MockNode parent;

    public HierarchicalMockNode(String path)
    {
        super(path);
        children = new HashMap<String, Node>();
    }

    public HierarchicalMockNode(String path, String type)
    {
        super(path, type);
        children = new HashMap<String, Node>();
    }

    @Override
    public Node addNode(String relPath)
    {
        String[] nodesToCreate = relPath.split("/");
        String localNodePath = nodesToCreate[0];
        String completeNodePath = (getPath() + "/"  + localNodePath).replace("//","/");

        HierarchicalMockNode n = new HierarchicalMockNode( completeNodePath );
        n.setParent(this);
        children.put(localNodePath, n);

        if ( nodesToCreate.length > 1 ) {
            n.addNode( relPath.replace( localNodePath + "/", ""));
        }

        return getNode(relPath);
    }

    @Override
    public Node getNode(String relPath)
    {
        String[] nodes = relPath.split("/");
        String firstNodePath = nodes[0];
        MockNode currentNode = (MockNode)children.get(firstNodePath);
        if ( nodes.length > 1 ) {
            currentNode = (MockNode)currentNode.getNode( relPath.replace( firstNodePath + "/", ""));
        }
        return currentNode;
    }

    @Override
    public Node addNode(String relPath, String primaryNodeTypeName)
    {
        return super.addNode(relPath,
                primaryNodeTypeName);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Node getParent()
    {
        return parent;
    }

    public void setParent( MockNode parent )
    {
        this.parent = parent;
    }
}
