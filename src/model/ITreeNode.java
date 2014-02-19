package model;

import java.util.ArrayList;

public interface ITreeNode {
	
	public ITreeNode getParent();
	public ITreeNode setParent();
	
	public ArrayList<ITreeNode> getChildren();
	public ITreeNode addChild();
	public ITreeNode removeChild();
}
