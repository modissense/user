package twitter;

import java.util.LinkedList;

import snetworks.SocialNetworkEntity;

public class GetTweets {

	private LinkedList<SocialNetworkEntity> entitiesToReturn;
	
	public LinkedList<SocialNetworkEntity> getTwitterUpdates(){
		return entitiesToReturn;
	}

}
