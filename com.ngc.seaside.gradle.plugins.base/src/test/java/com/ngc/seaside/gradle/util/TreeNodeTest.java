package com.ngc.seaside.gradle.util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class TreeNodeTest {

    @Test
    public void testAddChild() {
        String description = "root node";
        TreeNode root = new TreeNode(new TreePath("root"), description, new TreeNode.NameComparator());
        
        TreeNode c1 = new TreeNode(TreePath.fromString("root|c1"));
        TreeNode c2 = new TreeNode(TreePath.fromString("root|c2"));
        
        TreeNode c1a = new TreeNode(TreePath.fromString("root|c1|c1a"));
        TreeNode c1b = new TreeNode(TreePath.fromString("root|c1|c1b"));
        
        TreeNode c2a = new TreeNode(TreePath.fromString("root|c2|c2a"));
        TreeNode c2b = new TreeNode(TreePath.fromString("root|c2|c2b"));
        
        root.addChild(c1);
        root.addChild(c2);
        
        root.addChild(c1a);
        root.addChild(c1b);
        
        root.addChild(c2a);
        root.addChild(c2b);

        assertEquals(2, root.getChildren().size());
        assertEquals(2, c1.getChildren().size());
        assertEquals(2, c2.getChildren().size());
        assertEquals(description, root.getDescription());
    }
    
    @Test
    public void testGetHeight() {
        TreeNode root = new TreeNode(new TreePath("root"));
        
        TreeNode c1 = new TreeNode(TreePath.fromString("root|c1"));
        TreeNode c2 = new TreeNode(TreePath.fromString("root|c2"));
        
        TreeNode c1a = new TreeNode(TreePath.fromString("root|c1|c1a"));
        TreeNode c1b = new TreeNode(TreePath.fromString("root|c1|c1b"));
        
        TreeNode c2a = new TreeNode(TreePath.fromString("root|c2|c2a"));
        TreeNode c2b = new TreeNode(TreePath.fromString("root|c2|c2b"));
        
        root.addChild(c1);
        root.addChild(c2);
        
        c1.addChild(c1a);
        c1.addChild(c1b);
        
        c2.addChild(c2a);
        c2.addChild(c2b);
        
        assertEquals(3, root.getHeight());
        assertEquals(2, c1.getHeight());
        assertEquals(2, c2.getHeight());
        assertEquals(1, c1a.getHeight());
        assertEquals(1, c1b.getHeight());
        assertEquals(1, c2a.getHeight());
        assertEquals(1, c2b.getHeight());
        
    }
    
    @Test
    public void testGetLeafNodes() {
        TreeNode root = new TreeNode(new TreePath("root"));
        
        TreeNode c1 = new TreeNode(TreePath.fromString("root|c1"));
        TreeNode c2 = new TreeNode(TreePath.fromString("root|c2"));
        
        TreeNode c1a = new TreeNode(TreePath.fromString("root|c1|c1a"));
        TreeNode c1b = new TreeNode(TreePath.fromString("root|c1|c1b"));
        
        TreeNode c2a = new TreeNode(TreePath.fromString("root|c2|c2a"));
        TreeNode c2b = new TreeNode(TreePath.fromString("root|c2|c2b"));
        TreeNode c2b2 = new TreeNode(TreePath.fromString("root|c2|c2b|c2b2"));
        
        root.addChild(c1);
        root.addChild(c2);
        
        root.addChild(c1a);
        root.addChild(c1b);
        
        root.addChild(c2a);
        root.addChild(c2b);
        root.addChild(c2b2);
        
        Map<TreeNode, Integer> leafNodes = root.getLeafNodes();
        
        assertTrue(leafNodes.containsKey(c1a));
        assertTrue(leafNodes.containsKey(c1b));
        assertTrue(leafNodes.containsKey(c2a));
        assertTrue(leafNodes.containsKey(c2b2));
    }
    
    @Test
    public void testRemoveAll() {
        TreeNode root = new TreeNode(new TreePath("root"));
        
        TreeNode c1 = new TreeNode(TreePath.fromString("root|c1"));
        TreeNode c2 = new TreeNode(TreePath.fromString("root|c2"));
        
        TreeNode c1a = new TreeNode(TreePath.fromString("root|c1|c1a"));
        TreeNode c1b = new TreeNode(TreePath.fromString("root|c1|c1b"));
        
        TreeNode c2a = new TreeNode(TreePath.fromString("root|c2|c2a"));
        TreeNode c2b = new TreeNode(TreePath.fromString("root|c2|c2b"));
        TreeNode c2b2 = new TreeNode(TreePath.fromString("root|c2|c2b|c2b2"));
        
        root.addChild(c1);
        root.addChild(c2);
        
        root.addChild(c1a);
        root.addChild(c1b);
        
        root.addChild(c2a);
        root.addChild(c2b);
        root.addChild(c2b2);
        
        assertEquals(4, root.getHeight());
        
        root.removeAll();
        
        assertEquals(1, root.getHeight());
    }
    
}
