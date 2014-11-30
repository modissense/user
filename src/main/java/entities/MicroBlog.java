package entities;


import java.util.HashMap;
import java.util.List;


public class MicroBlog extends DatabaseTransaction {
	
	private Integer userid=-1;
	private String date;
	private Integer seqNumber=-1;
	private Integer poiID=-1;
	private String comment;
	private String arrived;
	private String off;
	private Boolean isPublic;

	public MicroBlog() {
		super();
	}
	
	@Override
	protected String getTableName() {
		return "sem_trajectory";
	}
	
	public Integer getUserID(){
		return userid;
	}
	
	public void setUserID(int userid){
		this.userid = userid;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public int getSeqNumber(){
		return seqNumber;
	}
	
	public void setSeqNumber(int seqnumber){
		this.seqNumber = seqnumber;
	}
	
	public int getPoiID(){
		return poiID;
	}
	
	public void setPoiID(int poiID){
		this.poiID = poiID;
	}
	
	public String getComment(){
		return comment;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getArrived(){
		return arrived;
	}
	
	public void setArrived(String arrived){
		this.arrived = arrived;
	}
	
	public String getOff(){
		return off;
	}
	
	public void setOff(String off){
		this.off = off;
	}
	
	public boolean getIsPublic(){
		return isPublic;
	}
	
	public void setIsPublic(boolean isPublic){
		this.isPublic = isPublic;
	}
	
	
	/**
	 * Put this blog object to database.
	 */
	public boolean create(){
		HashMap<String, String> foo = this.serialize();
		return this.create(foo);
	}
	
	public boolean updateBlog(Integer seq,Integer poid,String arrived,String off,String comment){
		HashMap<String, String> blogid = new HashMap<String, String>();
		boolean halfTransaction;
//		if(seq != null)
//			blogid.put("seq_number", seq.toString());
		if(poid!=null)
			blogid.put("poi_id", poid.toString());
		if(arrived!=null)
			blogid.put("arrived", "TIMESTAMP '"+arrived+"'");
		if(off!=null)
			blogid.put("off", "TIMESTAMP '"+off+"'");
		if(comment!=null)
			blogid.put("comment", "'"+comment+"'");
		if(poid==null&&arrived==null&off==null&comment==null)
			halfTransaction=true;
		else
			halfTransaction = this.update(blogid,this.serialize());
		if(halfTransaction&&(seq != null)){
			return swapPointsOfTrajectory(seq);
		}else
			return halfTransaction;
	}
	
	public boolean deleteBlog(){
		return this.delete(this.serialize());
	}
	/**
	 * Returns a HashMap of the object. Used for database transactions.
	 * @return
	 */
	private HashMap<String, String> serialize(){
		HashMap<String, String> foo = new HashMap<String, String>();
		if(this.userid!=-1)
			foo.put("user_id", this.userid.toString());
		if(this.date!=null)
			foo.put("date", "TIMESTAMP '"+this.date+"'");
		if(this.seqNumber!=-11111111)
			foo.put("seq_number", this.seqNumber.toString());
		if(this.poiID!=-1)
			foo.put("poi_id", this.poiID.toString());
		if(this.comment!=null)
			foo.put("comment", "'"+this.comment+"'");
		if(this.arrived!=null)
			foo.put("arrived", "TIMESTAMP '"+this.arrived+"'");
		if(this.off!=null)
			foo.put("off", "TIMESTAMP '"+this.off+"'");
		if(this.isPublic!=null)
			foo.put("public", Boolean.toString(this.isPublic));
		return foo;
	}
	
	public boolean isSynced(){
		return this.isSynced;
	}
	
	public List<HashMap<String, String>> read(){
		HashMap<String, String> t = this.serialize();
		
		List<HashMap<String, String>> res =this.read(t);
		if(res.size()==1){
			HashMap<String, String> info = res.get(0);
			this.userid=new Integer(info.get("user_id"));
			this.seqNumber=new Integer(info.get("seq_number"));
			this.date= info.get("date");
			this.arrived= info.get("arrived");
			this.off= info.get("off");
			this.poiID=new Integer(info.get("poi_id"));
			this.comment = info.get("comment");
			if(info.get("public").equals("t"))
				this.isPublic = true;
			else
				this.isPublic=false;
			this.isSynced=true;
		}
		return res;
	}
	
	public List<HashMap<String, String>> getBlogs(){
		String sql = "select date from sem_trajectory where user_id="+this.getUserID()+" group by date";
		List<HashMap<String, String>> res =this.executeSQLWithResults(sql);
		return res;
	}
	
	public List<HashMap<String, String>> getBlogTrajectories(int userID,String date){
		String sql = "select poi.poi_id, poi.name, poi.interest, poi.hotness, poi.publicity, poi.keywords, poi.description, poi.tmstamp, poi.deltmstamp, poi.user_id, ST_AsText(poi.geo)," +
				" temp.date, temp.seq_number, temp.comment, temp.arrived, temp.off, temp.public from (select * from sem_trajectory where user_id="+userID+" and date=TIMESTAMP '"+date+"') as temp, poi "+
				"where temp.poi_id = poi.poi_id order by seq_number";
		List<HashMap<String, String>> res =this.executeSQLWithResults(sql);
		return res;
	}
	
	public boolean swapPointsOfTrajectory(Integer newSeq){
		String sql = "select swap_seq_num("+this.userid+",'"+this.date+"',"+this.seqNumber+","+newSeq+");";
		System.out.println("execute: "+sql);
		return this.executeSQLWithoutResults(sql);
	}
	
}
