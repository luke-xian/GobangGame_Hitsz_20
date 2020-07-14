package bin;

import java.io.IOException;
//import java.util.concurrent.CopyOnWriteArraySet;
//import java.util.Random;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.util.ArrayList;
import java.util.Date;
//import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.InetSocketAddress;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/websocket/{userno}")
public class WebSocketTest {
    public static final int MaxUserPairs=100;//最大在线对战对数
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    //private static CopyOnWriteArraySet<WebSocketTest> webSocketSet = new CopyOnWriteArraySet<WebSocketTest>();
    private static ConcurrentHashMap<String, WebSocketTest> webSocketSet = new ConcurrentHashMap<String, WebSocketTest>();
    
    //记录比赛对阵
    private static ConcurrentHashMap<Integer, Match> MatchPairs = new ConcurrentHashMap<Integer, Match>();
    //记录选手与比赛id
    private static ConcurrentHashMap<String, Integer> User2MatchID = new ConcurrentHashMap<String, Integer>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //当前发消息的人员编号
    private String userno = "";
    
    private static String waitUser="";
    private static boolean[] MatchIDused=new boolean[MaxUserPairs];
    private static int MatchID=0;
     
    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     * @throws IOException 
     */
    @OnOpen
    public void onOpen(@PathParam(value = "userno") String param, Session session, EndpointConfig config) throws IOException {
        System.out.println(param);
        userno = param;//接收到发送消息的人员编号
        boolean flag= true;

    	InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
        System.out.println("有新连接加入！" + remoteAddress);   
       
        /*if(userno.equals("attack")) {
        	flag=false;
        }*/
        for (String key : webSocketSet.keySet()) {
        	if(param.equals(key)) {
        		System.out.println("昵称已存在");
        		session.getBasicRemote().sendText("message|System:昵称已存在");
        		flag =false;
        		break;
        	}
        }
        if(flag) {
            this.session = session;
            
            webSocketSet.put(param, this);//加入map中
            //addOnlineCount();           //在线数加1
            //System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
            System.out.println("有新连接加入！当前在线人数为" + webSocketSet.size());

             
            if(webSocketSet.size()%2==1){
            	waitUser=userno;
            	session.getBasicRemote().sendText("message|System:请等待你的对手");
            	System.out.println("当前等待的是:" + waitUser);
            	
            }
            if(webSocketSet.size()%2==0) {
            	if(!waitUser.equals("")) {
                	//sendAll("message|开始比赛");
                	
                	if(MatchIDused[MatchID]) {
                		MatchID=findMatchID(MatchID);
                	}
                	MatchIDused[MatchID]=true;
                	Match match=new Match(waitUser,userno);
                	MatchPairs.put(MatchID, match);
                	User2MatchID.put(waitUser, MatchID);
                	User2MatchID.put(userno, MatchID);
                	String begingame=MatchPairs.get(MatchID).BeginGame();              	                	
        			String user1=begingame.split("[|]")[0];
        			String user2=begingame.split("[|]")[1];
        			//int matchsuccess=Integer.parseInt(matchstatus.split("[|]")[2]);
        			int color=Integer.parseInt(begingame.split("[|]")[2]);
        			try {
        				webSocketSet.get(user1).sendMessage("message|System:开始比赛！");
        				webSocketSet.get(user2).sendMessage("message|System:开始比赛！");
        				webSocketSet.get(user1).sendMessage("message|System:您执"+(color>0?"白":"黑")+"子");
        				webSocketSet.get(user2).sendMessage("message|System:您执"+(color>0?"黑":"白")+"子");
        				webSocketSet.get(user1).sendMessage("message|System:您的对手是："+user2);
        				webSocketSet.get(user2).sendMessage("message|System:您的对手是："+user1);
        				webSocketSet.get(user1).sendMessage("message|System:您的回合");
    	        		webSocketSet.get(user2).sendMessage("message|System:对手的回合");
    	            } catch (IOException e) {
    	                e.printStackTrace();
    	            }                	                              	
                	
                	//System.out.println("当前比赛ID:"+MatchID);
                	//System.out.println(waitUser+"MatchID:"+User2MatchID.get(waitUser));
                	//System.out.println(userno+"MatchID:"+User2MatchID.get(userno));
                	
                	//BeginMatch();
            	}
            	waitUser="";
            }                        	        	        	
        }
      
}
    
    /**
     * 寻找空闲的MatchID
    */
    private int findMatchID(int matchID) {
		// TODO Auto-generated method stub
    	for(int count=0;count<MaxUserPairs;count++) {
    		matchID++;
    		matchID=matchID%MaxUserPairs;
    		if(!MatchIDused[matchID])return matchID;    			
    	}
		return -1;
	}


	/**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (!userno.equals("")&&  WebsocketUtil.getRemoteAddress(webSocketSet.get(userno).session).equals(WebsocketUtil.getRemoteAddress(session))) {
        	//String temp=""; 
        	 System.out.println("当前比赛数："+MatchPairs.size());
        	 webSocketSet.remove(userno);  //从set中删除
        	 
             //subOnlineCount();           //在线数减1
             //System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
             System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
             if(userno.equals(waitUser)) {
            	 waitUser="";
             }else{
            	 if(MatchPairs.get(User2MatchID.get(userno))!=null) {
	            	 if(MatchPairs.get(User2MatchID.get(userno)).GetMatchOn()) {
	            		System.out.println("有一用户已离线！游戏结束");
	            		String remainuser= MatchPairs.get(User2MatchID.get(userno)).AnotherUser(userno);
	        			try {  	        		       				
	    	        		webSocketSet.get(remainuser).sendMessage("message|System:您已获胜！对手已离线");
	    	            } catch (IOException e) {
	    	                e.printStackTrace();
	    	            }           		 
	            	 }else {
	                	 String anotheruser=MatchPairs.get(User2MatchID.get(userno)).AnotherUser(userno);
	                	 try {
	     					webSocketSet.get(anotheruser).sendMessage("message|System:您的对手已离线!");
	     				} catch (IOException e1) {
	     					// TODO Auto-generated catch block
	     					e1.printStackTrace();
	     				}              	 
	                 }
	            	 MatchPairs.remove(User2MatchID.get(userno));
                 }           	 
                 User2MatchID.remove(userno);           	 
             }
             
             System.out.println("当前比赛数："+MatchPairs.size());
        	
                  
            
        }
    }
    

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @SuppressWarnings("unused")
	@OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息:" + message);
        String status=message.split("[|]")[0];
        String location;
        int i,j;//t;
        //String temp=nowUser;
 
        if(status.equals("location")){
    		location=message.split("[|]")[1];
    		//System.out.println(location);
    		if(User2MatchID.get(userno)==null)return;
    		if(location.indexOf("onemore")<0){
	    		i=Integer.parseInt(location.split("[-]")[0]);
	    		j=Integer.parseInt(location.split("[-]")[1]);
	    		//System.out.println("当前比赛ID:" + User2MatchID.get(userno));
	    		System.out.println("当前比赛数："+MatchPairs.size());
	    		//System.out.println(MatchPairs.get(User2MatchID.get(userno)).IsNowUser(userno));
	    		
	    		if(MatchPairs.get(User2MatchID.get(userno)).IsNowUser(userno)) {
	    			String matchstatus=MatchPairs.get(User2MatchID.get(userno)).GetMatchStatus(i, j);
	    			String user1=matchstatus.split("[|]")[0];
	    			String user2=matchstatus.split("[|]")[1];
	    			int matchsuccess=Integer.parseInt(matchstatus.split("[|]")[2]);
	    			String matchcolor=Integer.parseInt(matchstatus.split("[|]")[3])>0?"w":"b";
	    			
	    			System.out.println("下一步是"+user1+"|上一步是"+user2+"下的是-"+i+"-"+j+"-"+matchcolor+"-"+matchsuccess);
	    			
	        		try {
	        			webSocketSet.get(user1).sendMessage(message+"-"+matchcolor);
						webSocketSet.get(user2).sendMessage(message+"-"+matchcolor);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			if(matchsuccess==-1) {
	    				try {  	        		
	    	        		webSocketSet.get(user1).sendMessage("message|System:您的回合");
	    	        		webSocketSet.get(user2).sendMessage("message|System:对手的回合");
	    	            } catch (IOException e2) {
	    	                e2.printStackTrace();
	    	            }    				    				
	    			}else {
	    				try {
	    	        		webSocketSet.get(user1).sendMessage("message|System:对手获胜！");
	    	        		webSocketSet.get(user2).sendMessage("message|System:恭喜您，您已获胜！");
	    	            } catch (IOException e3) {
	    	                e3.printStackTrace();
	    	            }   				    				
	    			}	      			
	    		}
    		}else {
    			if(webSocketSet.get(MatchPairs.get(User2MatchID.get(userno)).AnotherUser(userno))!=null) {
    				if(!MatchPairs.get(User2MatchID.get(userno)).GetMatchOn()) {
            			System.out.println(userno+"请求再来一局！");
            			try {
        					webSocketSet.get(MatchPairs.get(User2MatchID.get(userno)).AnotherUser(userno)).sendMessage("message|System:您的对手请求再来一局");
        				} catch (IOException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}
                    	String begingame=MatchPairs.get(MatchID).BeginGame();              	                	
            			String user1=begingame.split("[|]")[0];
            			String user2=begingame.split("[|]")[1];
            			//int matchsuccess=Integer.parseInt(matchstatus.split("[|]")[2]);
            			int color=Integer.parseInt(begingame.split("[|]")[2]);
            			try { 
            				webSocketSet.get(user1).sendMessage("message|System:开始比赛！");
            				webSocketSet.get(user2).sendMessage("message|System:开始比赛！");
            				webSocketSet.get(user1).sendMessage("message|System:您执"+(color>0?"白":"黑")+"子");
            				webSocketSet.get(user2).sendMessage("message|System:您执"+(color>0?"黑":"白")+"子");
            				//webSocketSet.get(user1).sendMessage("message|System:您的对手是："+user2);
            				//webSocketSet.get(user2).sendMessage("message|System:您的对手是："+user1);
            				webSocketSet.get(user1).sendMessage("message|System:您的回合");
        	        		webSocketSet.get(user2).sendMessage("message|System:对手的回合");
        	            } catch (IOException e) {
        	                e.printStackTrace();
        	            }  	 					
    				}else {
    					try {
							webSocketSet.get(userno).sendMessage("message|System:您的请求被拒绝");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}   					    					
    				}
			   				
    			}else {
    				try {
    					webSocketSet.get(userno).sendMessage("message|System:您的对手已离线!");
    				} catch (IOException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
    			}

    			    			
    		}
    		
    		
        }else {
        	sendAll(message);
        }  
    }
    


    /**
     * 给所有人发消息
     * @param message
     */
    private void sendAll(String message) {
        String now = getNowTime();
        String sendMessage="";// = message;//.split("[|]")[0];
        sendMessage=message+"-";
        for (String key : webSocketSet.keySet()) {
           // i++;
        	try {
                //判断接收用户是否是当前发消息的用户
                /*if (!userno.equals(key)) {
                    webSocketSet.get(key).sendMessage(now + "用户" + userno + "发来消息：" + " <br/> " + sendMessage);
                    System.out.println("key = " + key);
                    System.out.println("4");
                }*/
            	//webSocketSet.get(key).sendMessage(now + "用户" + userno + "发来消息：" + " <br/> " + sendMessage);
            	webSocketSet.get(key).sendMessage(sendMessage);
            	System.out.println(now + "|" + userno + ":" + sendMessage);
            	System.out.println("key = " + key);           	
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
    /**
     * 获取当前时间
     *
     * @return
     */
    private String getNowTime() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String time = format.format(date);
        return time;
    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketTest.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketTest.onlineCount--;
    }
}