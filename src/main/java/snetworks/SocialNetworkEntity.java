package snetworks;

import java.io.UnsupportedEncodingException;

/**
 * abstract class which represent any social network and all the relative information
 *  such as facebook/foursquare checkin, tweet, facebook status, etc.
 * @author giagulei
 *
 */
public abstract class SocialNetworkEntity {
	
	public abstract void parseBytes(byte[] bytes) throws UnsupportedEncodingException;
	public abstract byte[] getBytes()throws UnsupportedEncodingException;

}
