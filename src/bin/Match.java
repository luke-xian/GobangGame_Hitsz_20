package bin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Match {
	 public static final int BoardSize=15;//棋盘行数
    private int[][] oriData = new int[BoardSize][BoardSize];//记录棋盘位置
    private boolean MatchOn=false;//当前比赛进行
    private ArrayList<String> Userlog = new ArrayList<String> (); 
    private int color=-1;//棋子颜色
	
    public Match(String user1, String user2) {
    	
    	Userlog.add(user1);
    	Userlog.add(user2);
    	//System.out.println("Userlog里用户数:"+Userlog.size());  	
    	
    	
    }
	
    public String BeginGame() {
    	Initialize();
    	MatchOn=true;
    	Random r=new Random();
    	color=r.nextInt(100)%2;    	
    	System.out.println("新开始了一场比赛：由"+Userlog.get(color)+"对阵"+Userlog.get((color+1)%2)+"!");
    	return Userlog.get(color)+"|"+ Userlog.get((color+1)%2)+"|"+color;  	
    }
    
    public boolean IsNowUser(String userno) {
    	//System.out.println("Userlog里用户数:"+Userlog.size()+"----MatchOn"+MatchOn);
    	return Userlog.get(color).equals(userno);  	 	
    }
    
    public String GetMatchStatus(int i,int j){
		//System.out.println("color:"+color);
		oriData[j][i]=color;
		int success = isSuccess(i, j, color);
		//System.out.println(success);
		if(success!=-1)MatchOn=false;
		color=(color+1)%2;
    	return Userlog.get(color)+"|"+ Userlog.get((color+1)%2)+"|"+success+"|"+(color+1)%2;
    }
    
    public String AnotherUser(String userno) {
    	for(String Key:Userlog) {
    		if(!Key.equals(userno)) {
    			return Key;
    		}
    	}
    	return null; 
    }
    
    public boolean GetMatchOn() {
    	return MatchOn;   	
    }
    
    /**
     * 初始化对局
     *
     * 
     */   
    private void Initialize() {
    	System.out.println("已初始化！");
    	MatchOn=false;
        for (int x = 0; x < oriData.length; x++) {
            for (int y = 0; y < oriData[x].length; y++) {
                oriData[y][x] = -1;
            }
        } 
		/*for (int x = 0; x < oriData.length; x++) {
            for (int y = 0; y < oriData[x].length; y++) {
            	System.out.print(oriData[x][y]);    		
            }
            System.out.print('\n');
        }*/
        //Userlog.clear();
    }

    
    /**
     * 判断输赢
     *
     * 
     */   
    
    
    
    private int isSuccess(int x, int y, int f) {
        //y的范围在0-BoardSize之间，x的范围在0-24之间
        int count = 0;
        for (int i = x - 1; i > -1; i--) {
            if (oriData[y][i] != f) {
                break;
            }
            count++;
        }
        for (int i = x + 1; i < BoardSize; i++) {
            if (oriData[y][i] != f) {
                break;
            }
            count++;
        }
        if (count > 3) {
            return f;
        }
        count = 0;
        for (int i = y + 1; i < BoardSize; i++) {
            if (oriData[i][x] != f) {
                break;
            }
            count++;
        }
        for (int i = y - 1; i > -1; i--) {
            if (oriData[i][x] != f) {
                break;
            }
            count++;
        }
        if (count > 3) {
            return f;
        }
        count = 0;
        for (int i = x + 1, j = y + 1; i < BoardSize; i++, j++) {
            if (j < BoardSize) {
                if (oriData[j][i] != f) {
                    break;
                }
                count++;
            }else
            break;
        }
        for (int i = x - 1, j = y - 1; i > -1; i--, j--) {
            if (j > -1) {
                if (oriData[j][i] != f) {
                    break;
                }
                count++;
            }else
            break;
        }
        if (count > 3) {
            return f;
        }
        count = 0;
        for (int i = x + 1, j = y - 1; i < BoardSize; i++, j--) {
            if (j > -1) {
                if (oriData[j][i] != f) {
                    break;
                }
                count++;
            }else
            break;
        }
        for (int i = x - 1, j = y + 1; i > -1; i--, j++) {
            if (j < BoardSize) {
                if (oriData[j][i] != f) {
                    break;
                }
                count++;
            }else
            break;
        }
        if (count > 3) {
            return f;
        }
        return -1;
    }
	
}
